package io.confluent.developer.adventure;

public class StatusCommand {
  private final Instruction instruction;

  public StatusCommand(Instruction instruction) {
    this.instruction = instruction;
  }

  public Instruction getInstruction() {
    return instruction;
  }

  enum Instruction {
    INVENTORY, LOOK
  }
}
