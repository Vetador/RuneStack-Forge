package com.vetador.runestack.asm.mixin;

import com.vetador.runestack.RuneStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vetador.runestack.RuneStack.*;


@Mixin(GuiGraphics.class)
public abstract class RuneStackMixin {

    private static final Map<Long, ResourceLocation> COIN_TEXTURES = new HashMap<>();

    static {
        COIN_TEXTURES.put(1l, new ResourceLocation(RuneStack.MODID, "textures/item/one_coin.png"));
        COIN_TEXTURES.put(2l, new ResourceLocation(RuneStack.MODID, "textures/item/two_coins.png"));
        COIN_TEXTURES.put(3l, new ResourceLocation(RuneStack.MODID, "textures/item/three_coins.png"));
        COIN_TEXTURES.put(4l, new ResourceLocation(RuneStack.MODID, "textures/item/four_coins.png"));
        COIN_TEXTURES.put(5l, new ResourceLocation(RuneStack.MODID, "textures/item/five_coins.png"));
        COIN_TEXTURES.put(100l, new ResourceLocation(RuneStack.MODID, "textures/item/hundread_coins.png"));
        COIN_TEXTURES.put(1000l, new ResourceLocation(RuneStack.MODID, "textures/item/thousand_coins.png"));
        COIN_TEXTURES.put(10000l, new ResourceLocation(RuneStack.MODID, "textures/item/ten_thousands_coins.png"));
        COIN_TEXTURES.put(100000l, new ResourceLocation(RuneStack.MODID, "textures/item/hundread_thousands_coins.png"));
        COIN_TEXTURES.put(1000000l, new ResourceLocation(RuneStack.MODID, "textures/item/million_coins.png"));
        COIN_TEXTURES.put(10000000l, new ResourceLocation(RuneStack.MODID, "textures/item/ten_millions_coins.png"));
        COIN_TEXTURES.put(100000000l, new ResourceLocation(RuneStack.MODID, "textures/item/hundread_millions_coins.png"));
    }

    private static final Map<String, Integer> Durability_items = new HashMap<>();

    static {
        Durability_items.put("Medium Pouch", 90);
        Durability_items.put("Large Pouch", 60);
        Durability_items.put("Giant Pouch", 25);
        Durability_items.put("Cursed", 1000);
        Durability_items.put("Dharok", 5000);
        Durability_items.put("Ahrim", 5000);
        Durability_items.put("Verac", 5000);
        Durability_items.put("Torag", 5000);
        Durability_items.put("Karil", 5000);
        Durability_items.put("Guthan", 5000);

    }

    private static final Map<String, Integer> Charges_jewelry = new HashMap<>();

    static {
        Charges_jewelry.put("Games necklace", 8);
        Charges_jewelry.put("Gems necklace", 20);
        Charges_jewelry.put("Runecrafting necklace", 100);
        Charges_jewelry.put("Skills necklace", 8);
        Charges_jewelry.put("Ring of wealth", 5);
        Charges_jewelry.put("Ring of dueling", 8);
        Charges_jewelry.put("Amulet of glory", 4);
    }

    @Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V", at = @At("HEAD"), cancellable = true)
    private void renderItem(LivingEntity livingEntity, Level level, ItemStack itemStack, int i, int j, int k, CallbackInfo ci) {
        if (!itemStack.isEmpty() && featuresEnabled) {
            String itemName = itemStack.getHoverName().getString();
            if (itemName.contains("Coins")) {
                long itemCount = getItemCountFromTooltip(itemName);
                ResourceLocation texture = getTextureForItemCount(itemCount);
                if (resourceExists(texture)) {
                    ci.cancel();
                    GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
                    guiGraphics.pose().pushPose();
                    guiGraphics.blit(texture, i, j, 0, 0, 16, 16, 16, 16);
                    guiGraphics.pose().popPose();
                }
            }
        }
    }


    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "JUMP", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"), cancellable = true)
    private void onRenderItemDecorations(Font font, ItemStack itemStack, int x, int y, @Nullable String text, CallbackInfo ci) {

        //stack scale
        float scale = 0.53f;

        if (!itemStack.isEmpty() && featuresEnabled) {
            int n;
            int m;
            GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
            String itemName = itemStack.getHoverName().getString();
            String cleanItemName = itemName.replaceAll(" x\\d+$", "").trim();


            /**
             * Render durability bar on degradable items.
             */
            String key = Durability_items.keySet().stream()
                    .filter(cleanItemName::startsWith)
                    .findFirst()
                    .orElse(cleanItemName);

            if (Durability_items.containsKey(key) && isBarVisible(itemStack, key)) {
                guiGraphics.pose().pushPose();
                int k = getBarWidth(itemStack, key);
                int l = getBarColor(itemStack, key);
                m = x + 2;
                n = y + 13;
                guiGraphics.fill(RenderType.guiOverlay(), m, n, m + 13, n + 2, -16777216);
                guiGraphics.fill(RenderType.guiOverlay(), m, n, m + k, n + 1, l | 0xFF000000);
                guiGraphics.pose().popPose();
            }

            /**
             * Render diamonds as charge left on jewelry.
             */
            String key1 = Charges_jewelry.keySet().stream()
                    .filter(cleanItemName::startsWith)
                    .findFirst()
                    .orElse(cleanItemName);
            if (Charges_jewelry.containsKey(key1))
            {
                int l = getBarColorCharges(itemStack, key1);
                m = x + 2;
                n = y + 13;
                int maxDiamondCount = Math.max(getDiamondCount(key1), getDiamondCountLeft(itemStack, key1));
                float scale_diamonds = 0.4f;
                int scaledM = (int) (m / scale_diamonds);
                int scaledN = (int) (n / scale_diamonds);
                int totalWidth = maxDiamondCount * 4;
                int startX = scaledM + 17 - (totalWidth / 2);
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(scale_diamonds, scale_diamonds, 1);
                guiGraphics.pose().translate(0, 0, 200.0f);
                for (int i = 0; i < maxDiamondCount; i++) {
                    guiGraphics.drawString(font, "◆", startX + (i * 4), scaledN, -16777216);
                    if (i < getDiamondCountLeft(itemStack, key1)) guiGraphics.drawString(font, "◆", startX + (i * 4), scaledN, l | 0xFF000000);
                }
                guiGraphics.pose().popPose();
            }

            long itemCount = getItemCountFromTooltip(itemName);
            String formattedCount = formatCount(itemCount);
            int color = formatColor(itemCount);

            int textWidth = font.width(formattedCount);
            float originalX = x + (16 - textWidth * scale) / 2;
            float originalY = y + 6 + 3;
            int scaledX = (int) (originalX / scale);
            int scaledY = (int) (originalY / scale) - 16;

            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1);
            guiGraphics.pose().translate(0, 0, 200.0f);

            /**
             * Render slayer task stack.
             */
            boolean isSlayerTask = cleanItemName.endsWith("Slayer task");
            String taskCount = formatCount(getSlayerTaskCount(itemStack));
            if (isSlayerTask)
            {
                guiGraphics.drawString(font, taskCount, scaledX - 7, scaledY, color, true);
            }

            /**
             * Render stack.
             */
            guiGraphics.drawString(font, formattedCount, scaledX, scaledY, color, true);
            guiGraphics.pose().popPose();

            ci.cancel();
        }
    }

    public boolean isBarVisible(ItemStack itemStack, String key) {
        return getDurability(itemStack) < getMaxDurability(itemStack, key);
    }

    public int getDurability(ItemStack itemStack) {
        List<Component> tooltip = itemStack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        Pattern pattern = Pattern.compile("durabilit[yé]:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        for (Component line : tooltip) {
            String text = line.getString().replaceAll("§[0-9A-FK-OR]", "");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }

    public int getMaxDurability(ItemStack itemStack, String key)
    {
        if (itemStack.getItem() instanceof ArmorItem)
        {
            return (Durability_items.get(key) + 2500);
        }
        return Durability_items.get(key);
    }

    public int getBarWidth(ItemStack itemStack, String key) {
        int durability = getDurability(itemStack);
        int maxDurability = getMaxDurability(itemStack, key);
        int barWidth = Math.round((float)durability * 13.0f / (float)maxDurability);
        return barWidth;
    }

    public int getBarColor(ItemStack itemStack, String key) {
        int durability = getDurability(itemStack);
        int maxDurability = getMaxDurability(itemStack, key);
        float f = Math.max(0.0f, (float)durability / (float)maxDurability);
        float hue = Math.max(0, f / 2.0f - 0.12f);
        int barColor = Mth.hsvToRgb(hue, 1.0f, 1.0f);
        return barColor;
    }

    public int getCharge(ItemStack itemStack) {
        List<Component> tooltip = itemStack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        Pattern pattern = Pattern.compile("charges:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        for (Component line : tooltip) {
            String text = line.getString().replaceAll("§[0-9A-FK-OR]", "");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }

    public int getDiamondCount(String key)
    {
        int count = Charges_jewelry.get(key);
        if (count > 10) return 10;
        return count;
    }

    public int getDiamondCountLeft(ItemStack itemStack, String key)
    {
        int maxCharge = Charges_jewelry.get(key);
        int count = getCharge(itemStack);
        if (maxCharge > 10)
        {
            return (int) ((float) count / (float) maxCharge * 10);
        }
        return count;
    }

    public int getBarColorCharges(ItemStack itemStack, String key) {
        int charges = getCharge(itemStack);
        int maxCharges = Charges_jewelry.get(key);
        float f = Math.max(0.0f, (float)charges / (float)maxCharges);
        float hue = Math.max(0, f / 2.0f - 0.12f);
        if (hue > 0.38F) hue = 0.38F;
        int barColor = Mth.hsvToRgb(hue, 1.0f, 1.0f);
        return barColor;
    }

    public int getSlayerTaskCount(ItemStack itemStack) {
        List<Component> tooltip = itemStack.getTooltipLines(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips ? net.minecraft.world.item.TooltipFlag.Default.ADVANCED : net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        Pattern pattern = Pattern.compile("Kill \\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        for (Component line : tooltip) {
            String text = line.getString().replaceAll("§[0-9A-FK-OR]", "");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }

    private int getItemCountFromTooltip(String tooltip) {
        if (tooltip.contains("withdraw")){
            return 1;
        }
        Pattern pattern = Pattern.compile(".* [xX](\\d+)");
        Matcher matcher = pattern.matcher(tooltip);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 1;
    }

    private ResourceLocation getTextureForItemCount(long count) {
        if (count >= 100_000_000l) {
            return COIN_TEXTURES.get(100_000_000l);
        } else if (count >= 10_000_000l) {
            return COIN_TEXTURES.get(10_000_000l);
        } else if (count >= 1_000_000l) {
            return COIN_TEXTURES.get(1_000_000l);
        } else if (count >= 100_000l) {
            return COIN_TEXTURES.get(100_000l);
        } else if (count >= 10_000l) {
            return COIN_TEXTURES.get(10_000l);
        } else if (count >= 1_000l) {
            return COIN_TEXTURES.get(1_000l);
        } else if (count >= 100l) {
            return COIN_TEXTURES.get(100l);
        } else if (count >= 5l) {
            return COIN_TEXTURES.get(5l);
        } else if (count == 4l) {
            return COIN_TEXTURES.get(4l);
        } else if (count == 3l) {
            return COIN_TEXTURES.get(3l);
        } else if (count == 2l) {
            return COIN_TEXTURES.get(2l);
        } else if (count == 1l){
            return COIN_TEXTURES.get(1l);
        } else return null;
    }

    private String formatCount(long count) {
        if (count >= 10_000_000_000_000l) {
            return (count / 1_000_000_000_000l) + "T";
        } else if (count >= 10_000_000_000l) {
            return (count / 1_000_000_000l) + "B";
        } else if (count >= 10_000_000l) {
            return (count / 1_000_000l) + "M";
        } else if (count >= 100_000l) {
            return (count / 1_000l) + "K";
        } else  if (count > 1) {
            return String.valueOf(count);
        } else return null;
    }

    private int formatColor(long count) {
        if (count >= 10_000_000_000_000l) {
            return 0x33FFFF;
        } else if (count >= 10_000_000_000l) {
            return 0xFF33FF;
        } else if (count >= 10_000_000l) {
            return 0x00FF33;
        } else if (count >= 100_000l) {
            return 0xFFFFFF;
        } else {
            return 0xFFFF22;
        }
    }

    private static boolean resourceExists(ResourceLocation resourceLocation) {
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Optional<?> resource = resourceManager.getResource(resourceLocation);
        return resource.isPresent();
    }
}