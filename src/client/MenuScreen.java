package com.example.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

public class MenuScreen extends Screen {

    public MenuScreen() {
        super(Text.literal("Troll"));
    }

    private static Text aimLabel() {
        return Text.literal("Hello (Aim): " + (ExampleModClient.enabled ? "ON" : "OFF"));
    }

    private static Text rangeLabel() {
        return Text.literal("Range: " + ExampleModClient.range);
    }

    private static Text reachBtnLabel() {
        return Text.literal("Reach: " + (ExampleModClient.reachEnabled ? "ON" : "OFF"));
    }

    private static Text reachLabel() {
        return Text.literal("Reach distance: " + ExampleModClient.reach + " blocks");
    }

    @Override
    protected void init() {
        int w = 200, h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 60;

        // Aim lock on/off
        this.addDrawableChild(ButtonWidget.builder(aimLabel(), b -> {
            ExampleModClient.enabled = !ExampleModClient.enabled;
            b.setMessage(aimLabel());
        }).dimensions(x, y, w, h).build());

        // Aim range slider (1..50)
        this.addDrawableChild(new SliderWidget(x, y + 25, w, h, rangeLabel(),
                (ExampleModClient.range - 1) / 49.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(rangeLabel());
            }

            @Override
            protected void applyValue() {
                ExampleModClient.range = (int) Math.round(1 + this.value * 49);
            }
        });

        // Reach on/off
        this.addDrawableChild(ButtonWidget.builder(reachBtnLabel(), b -> {
            ExampleModClient.reachEnabled = !ExampleModClient.reachEnabled;
            b.setMessage(reachBtnLabel());
        }).dimensions(x, y + 60, w, h).build());

        // Reach distance slider (3..10 blocks, steps of 0.5)
        this.addDrawableChild(new SliderWidget(x, y + 85, w, h, reachLabel(),
                (ExampleModClient.reach - 3.0) / 7.0) {
            @Override
            protected void updateMessage() {
                this.setMessage(reachLabel());
            }

            @Override
            protected void applyValue() {
                ExampleModClient.reach = Math.round((3.0 + this.value * 7.0) * 2.0) / 2.0;
            }
        });
    }
}
