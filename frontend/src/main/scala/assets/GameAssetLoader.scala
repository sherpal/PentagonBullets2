package assets

import com.raquo.airstream.core.EventStream
import com.raquo.airstream.eventbus.EventBus
import org.scalablytyped.runtime.StringDictionary
import org.scalablytyped.runtime.StringDictionary.wrapStringDictionary
import typings.pixiJs.PIXI.LoaderResource
import typings.pixiJs.mod.Application
import typings.pixiJs.pixiJsStrings
import zio.{UIO, ZIO}
import typings.pixiJs.PIXI.Loader
import org.scalajs.dom

import scala.scalajs.js

/** The goal of the [[game.GameAssetLoader]] is simply to load all game assets, and warn the external world that it is
  * done, while also allowing to track the progress.
  */
final class GameAssetLoader(application: Application) {

  import GameAssetLoader.ProgressData

  private val progressBus: EventBus[ProgressData] = new EventBus
  private val endedBus: EventBus[Unit]            = new EventBus

  val assets: List[Asset] = (
    Asset.buffAssetMap.values ++
      Asset.abilityAssetMap.values ++
      Asset.units
  ).toList.distinct

  val $progressData: EventStream[ProgressData] = progressBus.events
  val endedLoadingEvent: EventStream[Unit]     = endedBus.events

  // todo: handle errors when loading
  val loadAssets: ZIO[Any, Nothing, PartialFunction[Asset, LoaderResource]] = for {
    fiber <- ZIO
      .effectAsync[Any, Nothing, StringDictionary[LoaderResource]] { callback =>
        assets
          .foldLeft(application.loader) { (loader, resourceUrl) =>
            loader.add(resourceUrl: String)
          }
          .load { (_, resources) =>
            callback(UIO(resources.asInstanceOf[StringDictionary[LoaderResource]]))
          }
          .on(
            "progress",
            (loader: Loader, resource: LoaderResource) =>
              progressBus.writer.onNext(ProgressData(loader.progress, resource.name))
          )
      }
      .fork
    resources <- fiber.join
    _         <- ZIO.effectTotal(endedBus.writer.onNext(()))
    fn <- UIO({
      case asset: Asset if resources.isDefinedAt(asset) => resources(asset)
    }: PartialFunction[Asset, LoaderResource])
  } yield fn

}

object GameAssetLoader {

  final case class ProgressData private (completion: Double, assetName: String)
  private object ProgressData {
    def apply(completion: Double, assetName: String): ProgressData = new ProgressData(completion, assetName)
  }

}
