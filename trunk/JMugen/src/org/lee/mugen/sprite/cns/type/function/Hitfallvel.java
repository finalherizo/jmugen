package org.lee.mugen.sprite.cns.type.function;

import org.lee.mugen.core.StateMachine;
import org.lee.mugen.parser.type.Valueable;
import org.lee.mugen.sprite.character.Sprite;
import org.lee.mugen.sprite.character.spiteCnsSubClass.VelSetSub;
import org.lee.mugen.sprite.character.spiteCnsSubClass.HitDefSub.Fall;
import org.lee.mugen.sprite.cns.eval.function.StateCtrlFunction;
import org.lee.mugen.sprite.parser.ExpressionFactory;

public class Hitfallvel extends StateCtrlFunction {

	public Hitfallvel() {
		super("hitfallvel", new String[] {});
	}
	@Override
	public Object getValue(String spriteId, Valueable... params) {
		Sprite sprite = StateMachine.getInstance().getSpriteInstance(spriteId);
		Fall fall = sprite.getInfo().getLastHitdef().getFall();
		
		VelSetSub vel = sprite.getInfo().getVelset();
		if (fall.getXvelocity() != null)
			vel.setX(fall.getXvelocity());
		vel.setY(fall.getYvelocity());
		
		return null;
	}
	
	public static Valueable[] parse(String name, String value) {
		String[] tokens = ExpressionFactory.expression2Tokens(value);
		return ExpressionFactory.evalExpression(tokens);
	}

}
