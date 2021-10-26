package io.confluent.developer.adventure;

// TODO I want this to be a record, but we can't use it until Gretty adopts Jetty v10+.
public class UseItemJoin {
  private final InventoryCommandValue inventoryCommand;
  private final Knapsack knapsack;
  private final LocationData locationData;
  private final ItemRulesValue itemRule;

  public UseItemJoin(InventoryCommandValue inventoryCommand, Knapsack knapsack, LocationData locationData,
      ItemRulesValue itemRule) {
    this.inventoryCommand = inventoryCommand;
    this.knapsack = knapsack;
    this.locationData = locationData;
    this.itemRule = itemRule;
  };

  public ItemRulesValue itemRule() {
    return itemRule;
  }

  public LocationData locationData() {
    return locationData;
  }

  public Knapsack knapsack() {
    return knapsack;
  }

  public InventoryCommandValue inventoryCommand() {
    return inventoryCommand;
  }

}
