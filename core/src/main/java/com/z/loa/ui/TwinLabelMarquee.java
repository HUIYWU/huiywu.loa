package com.z.loa.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.Group;

public class TwinLabelMarquee extends Group {
	private final Label leftLabel;
	private final Label rightLabel;
	
	private float speed = 120f;
	private float gap = 24f;
	private float stateTime = 0f;
	private boolean isPaused = true;//暂停滚动
	private boolean repeatEvenIfFits = false;
	private boolean disableRightLabel = false;
    private boolean fit = false;
	private Label.LabelStyle style;
	

	public TwinLabelMarquee(Label.LabelStyle style, String text) {
		this.leftLabel = new Label(text, style);
		this.rightLabel = new Label(text, style);
		this.style = style;
		super.addActor(leftLabel);
		super.addActor(rightLabel);

	}

	public void setText(String text) {
		leftLabel.setText(text);
		rightLabel.setText(text);
        if(checkFit()) {
        	disableRightLabel = true;
        }
		layoutLabels();
	}

	public void setText(String text, boolean disable_right_label) {
		this.disableRightLabel = disable_right_label;
        
		setText(text);
	}
    
    private boolean checkFit() {
    	float text_width = leftLabel.getPrefWidth();
		if (!repeatEvenIfFits && text_width <= super.getWidth()) {
            fit = true;
			return true;
		}
        fit = false;
        return false;
    }

	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public void setGap(float gap) {
		this.gap = gap;
	}
	
	public void setIsPaused() {
		this.stateTime = 0;
		this.isPaused = true;
	}
	
	private void layoutLabels() {
		float left_label_width = leftLabel.getPrefWidth();
		float right_label_width = left_label_width;
		leftLabel.setSize(left_label_width, super.getHeight());
		rightLabel.setSize(right_label_width, super.getHeight());
		if (disableRightLabel) {
			centerLabel(leftLabel.getWidth());
			rightLabel.setText("");
		} else {
			leftLabel.setPosition(0, 0);
			rightLabel.setPosition(left_label_width + gap, 0);
		}
	}

	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		leftLabel.setHeight(height);
		rightLabel.setHeight(height);
		layoutLabels();
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if (isPaused) {
			stateTime += delta;
		}
		if (stateTime >= 1.0f) {
			stateTime = 0f;
			isPaused = false;
		}
		//Gdx.app.log("pause?", "pause:" + isPaused);
		if (!isPaused) {
            if(fit) {
            	return;
            }
            
			leftLabel.moveBy(-speed * delta, 0);
			rightLabel.moveBy(-speed * delta, 0);

			if (leftLabel.getX() + leftLabel.getWidth() < 0) {
				isPaused = true;
				leftLabel.setX(rightLabel.getX() + rightLabel.getWidth() + gap);
			}
			if (rightLabel.getX() + rightLabel.getWidth() < 0) {
				isPaused = true;
				rightLabel.setX(leftLabel.getX() + leftLabel.getWidth() + gap);
			}
		}

	}

	private void centerLabel(float text_width) {
		float center = (super.getWidth() - text_width) * 0.5f;
		leftLabel.setPosition(center, 0);
		rightLabel.setPosition(center + text_width + gap, 0);
        rightLabel.setText("");
	}
}

