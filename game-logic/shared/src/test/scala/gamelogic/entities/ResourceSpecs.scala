package gamelogic.entities

final class ResourceSpecs extends munit.FunSuite {

  test("NoResource is smaller than some energy") {

    assert(Resource.noResourceAmount <= Resource.ResourceAmount(1, Resource.Energy))
    assert(Resource.noResourceAmount <= Resource.ResourceAmount(0, Resource.Energy))
    assert(Resource.ResourceAmount(1, Resource.Energy) >= Resource.noResourceAmount)
    assert(Resource.ResourceAmount(0, Resource.Energy) >= Resource.noResourceAmount)

  }

}
