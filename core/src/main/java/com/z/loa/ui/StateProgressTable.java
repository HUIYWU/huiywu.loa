package com.z.loa.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;

public class StateProgressTable extends Table {
    public ProgressBar progressBar;
    public Label progressLabel;
    private ProgressBar.ProgressBarStyle progressBarStyle;
    private Label.LabelStyle labelStyle;
    
    public StateProgressTable(float min, float max, float step, boolean vertical, ProgressBar.ProgressBarStyle p_style, Label.LabelStyle l_style) {
        this.progressBarStyle = p_style;
        this.labelStyle = l_style;
        this.progressBar = new ProgressBar(min, max, step, vertical, p_style);
        this.progressLabel = new Label((int)max + "/" + (int)max, l_style);
        init(max);
    }
    
    private void init(float max) {
        progressBar.setValue(max);
        progressBar.setAnimateDuration(0.2f);
        progressBar.setAnimateInterpolation(Interpolation.smooth);
        progressLabel.setAlignment(Align.center);
        this.left().bottom().add(progressLabel).expand().fill().row();
        this.left().bottom().add(progressBar).expand().fill();
    }

}
