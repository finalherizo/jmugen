package org.lee.mugen.sprite.cns.eval.trigger.function.spriteCns;

import org.lee.mugen.core.StateMachine;
import org.lee.mugen.parser.type.Valueable;
import org.lee.mugen.sprite.character.Sprite;
import org.lee.mugen.sprite.cns.eval.function.SpriteCnsTriggerFunction;
import org.lee.mugen.sprite.parser.Parser;
/**
 * 
 * @author Dr Wong
 * @category Trigger : Complete
 */
public class Sysfvar extends SpriteCnsTriggerFunction {

	public Sysfvar() {
		super("sysfvar", new String[0]);
	}
	@Override
	public Object getValue(String spriteId, Valueable... params) {
		Sprite sprite = StateMachine.getInstance().getSpriteInstance(spriteId);
		int index = Parser.getIntValue(params[0].getValue(spriteId));
		return sprite.getSpriteState().getVars().getSysFVar(index + "");
	}
}