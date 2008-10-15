package org.lee.mugen.sprite.cns.type.function;

import org.lee.mugen.core.StateMachine;
import org.lee.mugen.parser.type.Valueable;
import org.lee.mugen.sprite.cns.eval.function.StateCtrlFunction;
import org.lee.mugen.sprite.parser.ExpressionFactory;

public class Playerpush extends StateCtrlFunction {

    public Playerpush() {
        super("playerpush", new String[] {"value"});
    }
	public static Valueable[] parse(String name, String value) {
		String[] tokens = ExpressionFactory.expression2Tokens(value);
		return ExpressionFactory.evalExpression(tokens);
	}
	@Override
	public Object getValue(String spriteId, Valueable... params) {
		StateMachine.getInstance().getSpriteInstance(spriteId).getInfo().setPlayerpush(1);
		return null;
	}
	
	
}
