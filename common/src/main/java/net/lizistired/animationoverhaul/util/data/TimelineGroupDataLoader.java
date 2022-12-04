package net.lizistired.animationoverhaul.util.data;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.lizistired.animationoverhaul.AnimationOverhaulMain;
import net.lizistired.animationoverhaul.util.time.ChannelTimeline;
import gg.moonflower.pollen.api.registry.resource.PollinatedPreparableReloadListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class TimelineGroupDataLoader implements PollinatedPreparableReloadListener {

    //<Map<ResourceLocation, JsonElement>>

    private static final String FORMAT_VERSION = "0.2";

    public Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, Profiler profilerFiller) {
        Gson gson = new Gson();

        Collection<Identifier> passedFiles = resourceManager.findResources("timelinegroups", (string) -> {
            return string.getPath().endsWith(".json");
        }).keySet();

        //String entity = "bee";
        //EntityType<?> entityType = EntityType.byString(entity).isPresent() ? EntityType.byString(entity).get() : null;

        //System.out.println(EntityType.getKey(entityType)[1]);
        //Map<ResourceLocation, JsonElement> tempMap = null;

        //Iterate over each found resource location and put its JSON element into a map
        Map<Identifier, JsonElement> map = Maps.newHashMap();
        for(Identifier resourceLocation : passedFiles){
            String resourceLocationPath = resourceLocation.getPath();
            try {
                Resource resource = resourceManager.getResource(resourceLocation).get();
                try {
                    InputStream inputStream = resource.getInputStream();
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                        try {
                            JsonElement jsonElement = JsonHelper.deserialize(gson, reader, JsonElement.class);
                            if (jsonElement != null) {
                                map.put(resourceLocation, jsonElement);
                            } else {
                                AnimationOverhaulMain.LOGGER.error("Couldn't load data file {} as it's null or empty", resourceLocation);
                            }
                        } catch (Throwable bufferedReaderThrowable) {
                            try {
                                reader.close();
                            } catch (Throwable var16) {
                                bufferedReaderThrowable.addSuppressed(var16);
                            }
                            throw bufferedReaderThrowable;
                        }
                        reader.close();
                    } catch (Throwable inputStreamThrowable) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable closeInputStreamThrowable) {
                                inputStreamThrowable.addSuppressed(closeInputStreamThrowable);
                            }
                        }
                        throw inputStreamThrowable;
                    }
                    inputStream.close();
                } catch (Throwable resourceThrowable) {
                    if (resource != null) {
                        try {
                        } catch (Throwable closeResourceThrowable) {
                            resourceThrowable.addSuppressed(closeResourceThrowable);
                        }
                    }
                    throw resourceThrowable;
                }
            } catch (IOException e) {
                AnimationOverhaulMain.LOGGER.error("Error parsing data upon grabbing resource for resourceLocation " + resourceLocation);
            }
        }

        return map;
    }

    protected void apply(Map<Identifier, JsonElement> data, ResourceManager resourceManager, Profiler profilerFiller) {
        TimelineGroupData newData = new TimelineGroupData();
        for(Identifier resourceLocationKey : data.keySet()){
            JsonElement animationJSON = data.get(resourceLocationKey);


            String resourceNamespace = resourceLocationKey.toString().split(":")[0];
            String resourceBody = resourceLocationKey.toString().split(":")[1].split("\\.")[0].replace("timelinegroups/", "");
            Identifier finalResourceLocation = new Identifier(resourceNamespace, resourceBody);

            //String entityKey = resourceLocationKey.toString().split("/")[1];
            //String animationKey = resourceLocationKey.toString().split("/")[2].split("\\.")[0];
            float frameTime = animationJSON.getAsJsonObject().get("frame_length").getAsFloat() / 1.2F;
            String formatVersion;
            if(animationJSON.getAsJsonObject().has("format_version")){
                formatVersion = animationJSON.getAsJsonObject().get("format_version").getAsString();
            } else {
                formatVersion = "0.1";
            }

            if(Objects.equals(formatVersion, FORMAT_VERSION)){
                TimelineGroupData.TimelineGroup timelineGroup = new TimelineGroupData.TimelineGroup(frameTime);

                JsonArray partArrayJSON = animationJSON.getAsJsonObject().get("parts").getAsJsonArray();
                for(int partIndex = 0; partIndex < partArrayJSON.size(); partIndex++){
                    JsonObject partJSON = partArrayJSON.get(partIndex).getAsJsonObject();
                    String partName = partJSON.get("name").getAsString();
                    //AnimationOverhaul.LOGGER.info(partName);

                    ChannelTimeline channelTimeline = new ChannelTimeline();

                    JsonObject partKeyframesJSON = partJSON.get("keyframes").getAsJsonObject();
                    for(Map.Entry<String, JsonElement> keyframeEntry : partKeyframesJSON.entrySet()) {
                        int keyframeNumber = Integer.parseInt(keyframeEntry.getKey());
                        JsonElement keyframeJSON = keyframeEntry.getValue();
                        //AnimationOverhaul.LOGGER.info(keyframeNumber);

                        channelTimeline.addKeyframe(TransformChannel.x, keyframeNumber, keyframeJSON.getAsJsonObject().get("translate").getAsJsonArray().get(0).getAsFloat());
                        channelTimeline.addKeyframe(TransformChannel.y, keyframeNumber, keyframeJSON.getAsJsonObject().get("translate").getAsJsonArray().get(1).getAsFloat());
                        channelTimeline.addKeyframe(TransformChannel.z, keyframeNumber, keyframeJSON.getAsJsonObject().get("translate").getAsJsonArray().get(2).getAsFloat());

                        channelTimeline.addKeyframe(TransformChannel.xRot, keyframeNumber, keyframeJSON.getAsJsonObject().get("rotate").getAsJsonArray().get(0).getAsFloat());
                        channelTimeline.addKeyframe(TransformChannel.yRot, keyframeNumber, keyframeJSON.getAsJsonObject().get("rotate").getAsJsonArray().get(1).getAsFloat());
                        channelTimeline.addKeyframe(TransformChannel.zRot, keyframeNumber, keyframeJSON.getAsJsonObject().get("rotate").getAsJsonArray().get(2).getAsFloat());


                        /*
                        for(Map.Entry<String, JsonElement> attributeEntry : keyframeJSON.getAsJsonObject().entrySet()){
                            TransformChannel transformChannel = TransformChannel.valueOf(attributeEntry.getKey());
                            float keyframeValue = attributeEntry.getValue().getAsFloat();

                            channelTimeline = channelTimeline.addKeyframe(transformChannel, keyframeNumber, keyframeValue);
                            //AnimationOverhaul.LOGGER.info("Channel: {} Value: {}", transformChannel, keyframeValue);
                        }
                         */
                    }
                    timelineGroup.addPartTimeline(partName, channelTimeline);
                }


                newData.put(finalResourceLocation, timelineGroup);
                //AnimationOverhaul.LOGGER.info(frameTime);
                //AnimationOverhaul.LOGGER.info("Entity key: {} Animation key: {}", entityKey, animationKey);


                AnimationOverhaulMain.LOGGER.info("Successfully loaded animation {}", resourceLocationKey);
            } else {
                AnimationOverhaulMain.LOGGER.error("Failed to load animation {} (Animation format version was {}, not up to date with {})", resourceLocationKey, formatVersion, FORMAT_VERSION);
            }
        }

        TimelineGroupData.INSTANCE.clearAndReplace(newData);
    }

    @Override
    public Identifier getPollenId() {
        return new Identifier("timeline_group_loader");
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer preparationBarrier, ResourceManager resourceManager, Profiler profilerFiller, Profiler profilerFiller2, Executor executor, Executor executor2) {
        return CompletableFuture.supplyAsync(() -> this.prepare(resourceManager, profilerFiller), executor).<Object>thenCompose(preparationBarrier::whenPrepared).thenAcceptAsync(object -> this.apply((Map<Identifier, JsonElement>)object, resourceManager, profilerFiller2), executor2);
    }
}
