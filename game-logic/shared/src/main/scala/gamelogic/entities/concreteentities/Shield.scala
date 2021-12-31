package gamelogic.entities.concreteentities

import gamelogic.buffs.Buff.ResourceIdentifier
import gamelogic.buffs.{Buff, SimplePassiveBuff}
import gamelogic.entities.Entity
import gamelogic.gamestate.{GameAction, GameState}
import gamelogic.gamestate.gameactions.{
  PlayerHitByBullet,
  PlayerHitByMultipleBullets,
  PlayerHitBySmashBullet,
  PlayerTakeDamage
}
import gamelogic.utils.IdGeneratorContainer

/** While a Shield is active on a Player, he or she does not take any damage.
  */
final case class Shield(buffId: Buff.Id, appearanceTime: Long, bearerId: Entity.Id) extends SimplePassiveBuff {

  def resourceIdentifier: ResourceIdentifier = Buff.shield

  val duration: Long = 5000

  def actionTransformer(action: GameAction): List[GameAction] =
    List {
      action match {
        case PlayerTakeDamage(actionId, currentTime, plrId, srcId, _, source)
            if currentTime - appearanceTime <= duration && plrId == bearerId =>
          PlayerTakeDamage(actionId, currentTime, plrId, srcId, 0, source)
        case PlayerHitByBullet(actionId, plrId, bulletId, _, currentTime, source)
            if currentTime - appearanceTime <= duration && plrId == bearerId =>
          PlayerHitByBullet(actionId, plrId, bulletId, 0, currentTime, source)
        case PlayerHitByMultipleBullets(actionId, currentTime, bulletIds, plrId, _, source)
            if currentTime - appearanceTime <= duration &&
              bearerId == plrId =>
          PlayerHitByMultipleBullets(actionId, currentTime, bulletIds, plrId, 0, source)
        case PlayerHitBySmashBullet(actionId, currentTime, plrId, srcId, source)
            if currentTime - appearanceTime <= duration && plrId == bearerId =>
          PlayerTakeDamage(actionId, currentTime, plrId, srcId, 0, source)
        case _ =>
          action
      }
    }

  def endingAction(gameState: GameState, time: Long)(implicit
      idGeneratorContainer: IdGeneratorContainer
  ): List[GameAction] = List.empty

}
