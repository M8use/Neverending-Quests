package com.m8use.questlog.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

public final class QuestInstance {
   public String templateId;
   public int progress;
   public boolean claimed;
   /** Already-counted distinct keys (e.g. biome ids) for objectives like {@link ObjectiveType#VISIT_BIOME}. */
   public List<String> seenKeys = new ArrayList<>();

   public QuestInstance() {
   }

   public QuestInstance(String templateId, int progress, boolean claimed, List<String> seenKeys) {
      this.templateId = templateId;
      this.progress = progress;
      this.claimed = claimed;
      this.seenKeys = new ArrayList<>(seenKeys);
   }

   public QuestTemplate template() {
      return QuestTemplates.byId(this.templateId);
   }

   /** As {@link #template()}, but {@code null} instead of throwing if this instance's id is no longer in the pool. */
   public QuestTemplate templateOrNull() {
      return QuestTemplates.tryById(this.templateId);
   }

   public boolean isComplete() {
      return this.progress >= this.template().amount();
   }

   public static final Codec<QuestInstance> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            Codec.STRING.fieldOf("template_id").forGetter(q -> q.templateId),
            Codec.INT.fieldOf("progress").forGetter(q -> q.progress),
            Codec.BOOL.fieldOf("claimed").forGetter(q -> q.claimed),
            Codec.STRING.listOf().fieldOf("seen_keys").forGetter(q -> q.seenKeys)
         )
         .apply(instance, QuestInstance::new)
   );
}
