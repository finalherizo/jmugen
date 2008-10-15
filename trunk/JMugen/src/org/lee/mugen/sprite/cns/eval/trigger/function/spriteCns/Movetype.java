package org.lee.mugen.sprite.cns.eval.trigger.function.spriteCns;

import org.lee.mugen.core.StateMachine;
import org.lee.mugen.parser.type.Valueable;
import org.lee.mugen.sprite.cns.eval.function.SpriteCnsTriggerFunction;
/**
 * 
 * @author Dr Wong
 * @category Trigger : Complete
 */
public class Movetype extends SpriteCnsTriggerFunction {

	public Movetype() {
		super("movetype", new String[] {});
	}

	@Override
	public Object getValue(String spriteId, Valueable... params) {
		return StateMachine.getInstance().getSpriteInstance(spriteId).getInfo().getMovetype().toString().toLowerCase();
	}
	
	
}
