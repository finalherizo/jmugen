package org.lee.mugen.sprite.cns.type.function;

import org.lee.mugen.core.StateMachine;
import org.lee.mugen.parser.type.Valueable;
import org.lee.mugen.sprite.character.Sprite;
import org.lee.mugen.sprite.character.SpriteCns;
import org.lee.mugen.sprite.character.SpriteState;
import org.lee.mugen.sprite.cns.eval.function.StateCtrlFunction;
import org.lee.mugen.sprite.parser.ExpressionFactory;
import org.lee.mugen.sprite.parser.Parser;

public class Changestate extends StateCtrlFunction {
	public Changestate() {
		super("changestate", new String[] {"value", "ctrl", "anim"});
		setInterrupt(true);
	}

	@Override
	public Object getValue(String spriteId, Valueable... vs) {
		Sprite sprite = StateMachine.getInstance().getSpriteInstance(spriteId);
		SpriteState spriteState = sprite.getSpriteState();
		SpriteCns spriteInfo = sprite.getInfo();
		int valueIndex = getParamIndex("value");
		int ctrlIndex = getParamIndex("ctrl");
		int animIndex = getParamIndex("anim");
		
		Valueable value = valueableParams[valueIndex][0];
		Valueable ctrl = null;
		Valueable anim = null;
		
		if (valueableParams[ctrlIndex] != null && valueableParams[ctrlIndex].length > 0)
			ctrl = valueableParams[ctrlIndex][0];
		
		if (valueableParams[animIndex] != null)
			anim = valueableParams[animIndex][0];
		int ivalue = Parser.getIntValue(value.getValue(spriteId));

		
		if (ctrl != null) {
			int ictrl = Parser.getIntValue(ctrl.getValue(spriteId));
			spriteInfo.setCtrl(ictrl);
		}
		if (anim != null) {
			int ianim = Parser.getIntValue(anim.getValue(spriteId));
			sprite.getSprAnimMng().setAction(ianim);
		}
		spriteState.changeStateDef(ivalue);
		return null;
	}
	public static Valueable[] parse(String name, String value) {
		String[] tokens = ExpressionFactory.expression2Tokens(value);
		return ExpressionFactory.evalExpression(tokens);
	}
}
