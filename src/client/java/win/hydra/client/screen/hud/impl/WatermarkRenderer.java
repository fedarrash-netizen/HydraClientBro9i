package win.hydra.client.screen.hud.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import win.hydra.client.Client;
import win.hydra.client.api.client.Constants;
import win.hydra.client.api.events.orbit.EventHandler;
import win.hydra.client.event.Render2DEvent;
import win.hydra.client.hud.IRenderer;
import win.hydra.client.managers.module.impl.render.InterFace;
import win.hydra.client.util.animation.NumberTransition;
import win.hydra.client.util.math.Mathf;
import win.hydra.client.util.player.MoveUtil;
import win.hydra.client.util.player.PlayerUtil;
import win.hydra.client.util.render.color.ColorFormatting;
import win.hydra.client.util.render.color.ColorUtil;
import win.hydra.client.util.render.draw.RectUtil;
import win.hydra.client.util.render.draw.RenderUtil;
import win.hydra.client.util.render.font.Fonts;
import win.hydra.client.util.render.gif.GifRender;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class WatermarkRenderer implements IRenderer {

    private float title_width = 50;
    private float username_width = 100;
    private float server_width = 100;
    private float fps_width = 50;
    private float ping_width = 50;

    private float tempFps = 0;
    private float tempPing = 0;

    @SuppressWarnings("unused")
    private final GifRender gifRender = new GifRender(ResourceLocation.fromNamespaceAndPath("hydra", "texture/texture_wt.gif"));

    private final Minecraft mc = Minecraft.getInstance();

    @EventHandler
    @Override
    public void render(Render2DEvent e) {
        GuiGraphics g = e.getGraphics();

        float pos = 8;

        int speed = 5;
        int intex = 24;
        pos = 5;
        float pos2 = 5;
        String nameWT = Constants.RELEASE;
        StringBuilder fadeUsername = new StringBuilder();

        // top bar
        if (InterFace.getInstance().wtnameSF.is("Alpha")) nameWT = "Alpha";

        for (int i = 0; i < nameWT.length(); i++) {
            fadeUsername.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                    InterFace.getInstance().themeColor(),
                    ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
            fadeUsername.append(nameWT.charAt(i));
        }

        String nameWT1 = "P";
        StringBuilder fadeUsername1 = new StringBuilder();

        for (int i = 0; i < nameWT1.length(); i++) {
            fadeUsername1.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                    InterFace.getInstance().themeColor(),
                    ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
            fadeUsername1.append(nameWT1.charAt(i));
        }

        InterFace.getInstance().drawClientRect(g, pos2, pos,
                26.5F + Fonts.SFP_MEDIUM.getWidth(nameWT, 7), 14.5F, 1, 3.5F);

        Fonts.ICON_NURIK.draw(g, fadeUsername1.toString(), pos2 + 4.4F, pos + 4, -1, 8);

        RectUtil.drawRect(g, pos2 + 17F, pos + 3.5F, 0.5F, 7, ColorUtil.multAlpha(-1, 0.1F));

        Fonts.SFP_MEDIUM.draw(g, fadeUsername.toString(), pos2 + 22F, pos + 3.5F, -1, 7);


        String nameWT12 = "W";
        StringBuilder fadeUsername12 = new StringBuilder();

        for (int i = 0; i < nameWT12.length(); i++) {
            fadeUsername12.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                    InterFace.getInstance().themeColor(),
                    ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
            fadeUsername12.append(nameWT12.charAt(i));
        }

        String nameWT123 = "X";
        StringBuilder fadeUsername123 = new StringBuilder();

        for (int i = 0; i < nameWT123.length(); i++) {
            fadeUsername123.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                    InterFace.getInstance().themeColor(),
                    ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
            fadeUsername123.append(nameWT123.charAt(i));
        }

        String nameWT1234 = "V";
        StringBuilder fadeUsername1234 = new StringBuilder();

        for (int i = 0; i < nameWT1234.length(); i++) {
            fadeUsername1234.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                    InterFace.getInstance().themeColor(),
                    ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
            fadeUsername1234.append(nameWT1234.charAt(i));
        }


        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = currentTime.format(formatter);


        pos2 = pos2 + 26.5F + 2 + Fonts.SFP_MEDIUM.getWidth(nameWT, 7);
        tempFps = NumberTransition.result(tempFps, mc.getFps());
        String fps = ((int) tempFps) + " Fps";

        float addx = 0;
        float addx2 = 0;
        float addx23 = 0;


        if (InterFace.getInstance().wtElement.getValue("Время")) {
            addx += 11 + Fonts.SFP_MEDIUM.getWidth(formattedTime, 7) + 7;
        }
        if (InterFace.getInstance().wtElement.getValue("Фпс")) {
            addx2 += 18.5F + Fonts.SFP_MEDIUM.getWidth(fps, 7);
            addx += Fonts.SFP_MEDIUM.getWidth(fps, 7) + 4 + 10 + 5;
        }
        if (InterFace.getInstance().wtElement.getValue("Юзер найм")) {
            addx += Fonts.SFP_MEDIUM.getWidth(InterFace.getInstance().namewatermerk.getValue(), 7) + 20;
            addx23 += Fonts.SFP_MEDIUM.getWidth(InterFace.getInstance().namewatermerk.getValue(), 7) + 20;
        }

        InterFace.getInstance().drawClientRect(g, pos2, pos, addx, 14.5F, 1, 3.5F);

        if (InterFace.getInstance().wtElement.getValue("Фпс")) {

            Fonts.ICON_NURIK.draw(g, fadeUsername123.toString(), pos2 + 4 + addx23, pos + 4.2F, -1, 7);

            Fonts.SFP_MEDIUM.draw(g, fps, pos2 + 15 + addx23, pos + 3.7F, -1, 7);

            if (InterFace.getInstance().wtElement.getValue("Юзер найм")) {

                RectUtil.drawRect(g, pos2 + addx23 - 0.5F, pos + 3.4F, 0.5F, 7, ColorUtil.multAlpha(-1, 0.1F));
            }
        }

        if (InterFace.getInstance().wtElement.getValue("Юзер найм")) {

            Fonts.ICON_NURIK.draw(g, fadeUsername12.toString(), pos2 + 4.5F, pos + 4.2F, -1, 7);

            Fonts.SFP_MEDIUM.draw(g, InterFace.getInstance().namewatermerk.getValue(), pos2 + 15, pos + 3.7F, -1, 7);
        }

        //38
        if (InterFace.getInstance().wtElement.getValue("Время")) {
            if (InterFace.getInstance().wtElement.getValue("Юзер найм") || InterFace.getInstance().wtElement.getValue("Фпс")) {

                RectUtil.drawRect(g, pos2 + addx2 + addx23 - 0.5F, pos + 3.4F, 0.5F, 7, ColorUtil.multAlpha(-1, 0.1F));
            }
            Fonts.ICON_NURIK.draw(g, fadeUsername1234.toString(), pos2 + 4 + addx2 + addx23 - 0.5F, pos + 4.2F, -1, 7);


            Fonts.SFP_MEDIUM.draw(g, formattedTime, pos2 + 14 + addx2 + addx23 - 0.5F, pos + 3.7F, -1, 7);
        }


        /// нижняя полоса
        pos = 5 + 14.5F + 2;

        pos2 = 5;
        nameWT1 = "U";
        fadeUsername1 = new StringBuilder();

        for (int i = 0; i < nameWT1.length(); i++) {
            fadeUsername1.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                    InterFace.getInstance().themeColor(),
                    ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
            fadeUsername1.append(nameWT1.charAt(i));
        }

        if (InterFace.getInstance().wtnameSF.is("Alpha")) {

            InterFace.getInstance().drawClientRect(g, pos2, pos, 17, 14.5F, 1, 3.5F);

            Fonts.ICON_NURIK.draw(g, fadeUsername1.toString(), pos2 + 4.2F, pos + 4, -1, 7.5F);
            pos2 += 17 + 2;

        }

        nameWT1 = "F";
        fadeUsername1 = new StringBuilder();

        for (int i = 0; i < nameWT1.length(); i++) {
            fadeUsername1.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                    InterFace.getInstance().themeColor(),
                    ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
            fadeUsername1.append(nameWT1.charAt(i));
        }
        String bps = (int) mc.player.getX() + ", " + (int) mc.player.getY() + ", " + (int) mc.player.getZ();

        if (InterFace.getInstance().wtElement.getValue("Кординаты")) {

            InterFace.getInstance().drawClientRect(g, pos2, pos,
                    27 + Fonts.SFP_MEDIUM.getWidth(bps, 7), 14.5F, 1, 3.5F);

            Fonts.ICON_NURIK.draw(g, fadeUsername1.toString(), pos2 + 4.2F, pos + 4.05F, -1, 8);

            RectUtil.drawRect(g, pos2 + 17, pos + 3.4F, 0.5F, 7, ColorUtil.multAlpha(-1, 0.1F));

            Fonts.SFP_MEDIUM.draw(g, bps, pos2 + 22, pos + 3.7F, -1, 7);


            pos2 += 27 + Fonts.SFP_MEDIUM.getWidth(bps, 7) + 2;
        }
        if (InterFace.getInstance().wtElement.getValue("Пинг")) {

            nameWT1 = "Q";
            fadeUsername1 = new StringBuilder();

            bps = PlayerUtil.getPing() + " Ping";

            for (int i = 0; i < nameWT1.length(); i++) {
                fadeUsername1.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                        InterFace.getInstance().themeColor(),
                        ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
                fadeUsername1.append(nameWT1.charAt(i));
            }

            InterFace.getInstance().drawClientRect(g, pos2, pos,
                    27 + Fonts.SFP_MEDIUM.getWidth(bps, 7), 14.5F, 1, 3.5F);

            Fonts.ICON_NURIK.draw(g, fadeUsername1.toString(), pos2 + 4.2F, pos + 4.05F, -1, 8);

            RectUtil.drawRect(g, pos2 + 17, pos + 3.4F, 0.5F, 7, ColorUtil.multAlpha(-1, 0.1F));

            Fonts.SFP_MEDIUM.draw(g, bps, pos2 + 22, pos + 3.7F, -1, 7);


            pos2 += 27 + Fonts.SFP_MEDIUM.getWidth(bps, 7) + 2;
        }
        if (InterFace.getInstance().wtElement.getValue("Тпс")) {
            nameWT1 = "T";
            fadeUsername1 = new StringBuilder();

            bps = Mathf.round(Client.inst().serverTps().getTPS(), 1) + " Ticks";

            for (int i = 0; i < nameWT1.length(); i++) {
                fadeUsername1.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                        InterFace.getInstance().themeColor(),
                        ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
                fadeUsername1.append(nameWT1.charAt(i));
            }

            InterFace.getInstance().drawClientRect(g, pos2, pos,
                    27 + Fonts.SFP_MEDIUM.getWidth(bps, 7), 14.5F, 1, 3.5F);

            // RenderUtil.drawImage can be implemented later if you want icons
            RenderUtil.drawImage(ResourceLocation.fromNamespaceAndPath("hydra", "interface/tps.png"),
                    g, pos2 + 4.2F, pos + 3.2F, 8, 8,
                    ColorUtil.fade(4, 0, InterFace.getInstance().themeColor(),
                            ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F)));

            RectUtil.drawRect(g, pos2 + 17, pos + 3.4F, 0.5F, 7, ColorUtil.multAlpha(-1, 0.1F));

            Fonts.SFP_MEDIUM.draw(g, bps, pos2 + 22, pos + 3.7F, -1, 7);

            pos2 += 27 + Fonts.SFP_MEDIUM.getWidth(bps, 7) + 2;
        }
        if (InterFace.getInstance().wtElement.getValue("Бпс")) {
            nameWT1 = "T";
            fadeUsername1 = new StringBuilder();

            bps = Mathf.round(MoveUtil.speedSqrt() * 20.0F, 1) + " Bps";

            for (int i = 0; i < nameWT1.length(); i++) {
                fadeUsername1.append(ColorFormatting.getColor(ColorUtil.fade(speed, intex * i,
                        InterFace.getInstance().themeColor(),
                        ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F))));
                fadeUsername1.append(nameWT1.charAt(i));
            }

            InterFace.getInstance().drawClientRect(g, pos2, pos,
                    27 + Fonts.SFP_MEDIUM.getWidth(bps, 7), 14.5F, 1, 3.5F);

            RenderUtil.drawImage(ResourceLocation.fromNamespaceAndPath("hydra", "interface/bps.png"),
                    g, pos2 + 4.2F, pos + 3.2F, 8, 8,
                    ColorUtil.fade(4, 0, InterFace.getInstance().themeColor(),
                            ColorUtil.multDark(InterFace.getInstance().themeColor(), 0.45F)));

            RectUtil.drawRect(g, pos2 + 17, pos + 3.4F, 0.5F, 7, ColorUtil.multAlpha(-1, 0.1F));

            Fonts.SFP_MEDIUM.draw(g, bps, pos2 + 22, pos + 3.7F, -1, 7);
        }
    }
}
