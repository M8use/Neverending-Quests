package com.m8use.questlog.quest;

public enum QuestRarity {
   COMMON("gui.questlog.rarity.common", 0xFF9AA0B0, 0xFF6B7080),
   UNCOMMON("gui.questlog.rarity.uncommon", 0xFF3FCB6B, 0xFF1E9E4A),
   RARE("gui.questlog.rarity.rare", 0xFF3F9BFF, 0xFF1D6FE0),
   EPIC("gui.questlog.rarity.epic", 0xFFB24EFF, 0xFF8318E0),
   LEGENDARY("gui.questlog.rarity.legendary", 0xFFFFB020, 0xFFE88A00),
   MYTHIC("gui.questlog.rarity.mythic", 0xFFFF4FA3, 0xFF4FD6FF);

   private final String translationKey;
   private final int color;
   private final int glowColor;

   QuestRarity(String translationKey, int color, int glowColor) {
      this.translationKey = translationKey;
      this.color = color;
      this.glowColor = glowColor;
   }

   public String translationKey() {
      return this.translationKey;
   }

   public int color() {
      return this.color;
   }

   public int glowColor() {
      return this.glowColor;
   }

   /** True for {@link #MYTHIC}, the final rarity: it cannot be upgraded further. */
   public boolean isMaxRarity() {
      return this == MYTHIC;
   }

   /**
    * The rarity that {@link #UPGRADE_COST} boxes of this rarity convert into, or {@code null} for
    * {@link #MYTHIC} (which has no higher tier).
    */
   @org.jetbrains.annotations.Nullable
   public QuestRarity next() {
      QuestRarity[] values = values();
      int nextOrdinal = this.ordinal() + 1;
      return nextOrdinal < values.length ? values[nextOrdinal] : null;
   }

   /** How many boxes of one rarity are consumed to produce a single box of the next rarity up. */
   public static final int UPGRADE_COST = 10;
}
