package gamelogic.entities

import gamelogic.utils.IdGeneratorContainer

/** An entity is anything that exist in the game. */
trait Entity {

  val id: Entity.Id

  /** Used to distinguish entities that can hurt (or help) other entities.
    *
    * Typically, a direct damage on a target can be dealt only to a member not in the team. I the same fashion, an
    * ability which heals a given target will typically only target people on the same team. This is a rule of thumb and
    * does not need to be true in general. Abilities could hurt within the same team, or help across teams.
    *
    * There should typically be 3 teams:
    *   - one for the players
    *   - one for the ai
    *   - and a "dummy" one for neutral entities (such as basic obstacles, for example).
    *
    * Currently the implementation enforces this fact in the code. This could potentially change in the future.
    */
  //def teamId: Entity.TeamId

  /** Time at which the entity was last modified. */
  def time: Long

}

object Entity {

  opaque type Id = Long

  object Id:
    extension (id: Id) @inline def toLong: Long = id: Long

    def apply(long: Long): Id = long

    def initial: Id = 0L

    given Ordering[Id] = Ordering.fromLessThan(_ < _)

  def newId()(using gen: IdGeneratorContainer): Entity.Id = gen.entityIdGenerator.nextId()

  val dummyEntityId: Id = -1L // This id can be used wherever an entity id is required, but meaningless.

  type TeamId = Int

  object teams {
    final val neutralTeam: TeamId = 0
    final val playerTeam: TeamId  = 1
    final val mobTeam: TeamId     = 2
  }

}
