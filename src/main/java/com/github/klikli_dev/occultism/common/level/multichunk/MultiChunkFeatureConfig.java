/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.common.level.multichunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class MultiChunkFeatureConfig implements FeatureConfiguration {

    public static final Codec<MultiChunkFeatureConfig> CODEC = RecordCodecBuilder.create((kind1) -> {
        //CODEC = codec
        return kind1.group(
                Codec.intRange(0, 16).fieldOf("max_chunks_to_root").forGetter((config) -> {
                    return config.maxChunksToRoot;
                }), Codec.intRange(0, 10000).fieldOf("chance_to_generate").forGetter((config) -> {
                    return config.chanceToGenerate;
                }), Codec.intRange(0, 512).fieldOf("min_generation_height").forGetter((config) -> {
                    return config.minGenerationHeight;
                }), Codec.intRange(0, 512).fieldOf("max_generation_height").forGetter((config) -> {
                    return config.maxGenerationHeight;
                }), Codec.intRange(0, Integer.MAX_VALUE).fieldOf("feature_seed_salt").forGetter((config) -> {
                    return config.featureSeedSalt;
                }), Codec.list(TagKey.codec(ForgeRegistries.BIOMES.getRegistryKey())).fieldOf("biome_tag_blacklist").forGetter((config) -> {
                    return config.biomeTagBlacklist;
                })
        ).apply(kind1, MultiChunkFeatureConfig::new);
    });
    /**
     * The maximum amount of chunks from the root position to still generate this feature.
     */
    public final int maxChunksToRoot;
    public final int chanceToGenerate;
    public final int minGenerationHeight;
    public final int maxGenerationHeight;
    public final int featureSeedSalt;
    public final List<TagKey<Biome>> biomeTagBlacklist;


    public MultiChunkFeatureConfig(int maxChunksToRoot, int chanceToGenerate, int minGenerationHeight,
                                   int maxGenerationHeight, int featureSeedSalt, List<TagKey<Biome>> biomeTagBlacklist) {
        this.maxChunksToRoot = maxChunksToRoot;
        this.chanceToGenerate = chanceToGenerate;
        this.featureSeedSalt = featureSeedSalt;
        this.minGenerationHeight = minGenerationHeight;
        this.maxGenerationHeight = maxGenerationHeight;
        this.biomeTagBlacklist = biomeTagBlacklist;
    }
}
