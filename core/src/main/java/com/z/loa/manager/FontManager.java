package com.z.loa.manager;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;

public class FontManager {
	private static String charRepertoire = "";//"状态技能物品装备系统角色金气上中下底部音乐设置游戏信息储存进度存档盘测试战斗离开：无空是否行动 ";
	public static BitmapFont font;
	private static FreeTypeFontGenerator generator;
    private static FreeTypeFontGenerator.FreeTypeFontParameter parameter;
	
	static {
		generator = new FreeTypeFontGenerator(Gdx.files.internal("zhengti.ttf"));
		charRepertoire += FreeTypeFontGenerator.DEFAULT_CHARS;
	}
	
	public static BitmapFont getFont() {
		if (font == null) {
			generateFont();
            font.getData().markupEnabled = true;
		}
		
		return font;
	}
	@Deprecated
	public static boolean updateFont(String text) {
        if(text == null) {
            return false;
        }
		StringBuilder builder = new StringBuilder();
		for (char c: text.toCharArray()) {
			if (charRepertoire.indexOf(c) == -1) {
				builder.append(c);
			}
		}
		String temp = new String(charRepertoire);
		charRepertoire += builder;
		if (charRepertoire.equals(temp)) {
			return false;
		}
		//generateFont();
		return true;
		
	}
	
	private static void generateFont() {
		parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = 45;
		parameter.color = Color.WHITE;
		parameter.borderWidth = 1.0f;
		parameter.borderColor = Color.BLACK;
		parameter.incremental = true;
		font = generator.generateFont(parameter);
	}
    
    public static void disposeAll() {
        generator.generateData(parameter).dispose();
        generator.dispose();
    }
}
