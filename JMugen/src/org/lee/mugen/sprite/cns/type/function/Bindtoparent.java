package org.lee.mugen.sprite.cns.type.function;

import org.lee.mugen.core.StateMachine;
import org.lee.mugen.parser.type.Valueable;
import org.lee.mugen.sprite.character.Sprite;
import org.lee.mugen.sprite.character.SpriteHelper;
import org.lee.mugen.sprite.cns.eval.function.StateCtrlFunction;
import org.lee.mugen.sprite.entity.BindToParentSub;
import org.lee.mugen.sprite.entity.BindToSub;
import org.lee.mugen.sprite.entity.PointF;
import org.lee.mugen.sprite.parser.ExpressionFactory;

public class Bindtoparent extends StateCtrlFunction {

    // TODO : bindtoparent
    public Bindtoparent() {
        super("bindtoparent", new String[] {"time", "facing", "pos"});
    }
    @Override
    public Object getValue(String spriteId, Valueable... params) {
    	Sprite sprite = StateMachine.getInstance().getSpriteInstance(spriteId);
    	if (sprite instanceof SpriteHelper) {
    		final Sprite parent = StateMachine.getInstance().getParent(sprite);
    		final BindToParentSub parentSub = new BindToParentSub();
    		
    		fillBean(spriteId, parentSub);
    		BindToSub sub = new BindToSub() {

				@Override
				public PointF getPos() {
					return new PointF(parent.getInfo().getXPos() + 
			    	    	(parent.isFlip()? -parentSub.getPos().getX(): parentSub.getPos().getX()),
			    	    	parent.getInfo().getYPos() + parentSub.getPos().getY()
			    	    	);
				}
    			
    		};
    		sub.setCaller(StateMachine.getInstance().getSpriteInstance(spriteId));
    	    
    	    
    	    
    	    sprite.getInfo().setBindTo(sub);
    	}
    	return null;
    }
	public static Valueable[] parse(String name, String value) {
		String[] tokens = ExpressionFactory.expression2Tokens(value);
		Valueable[] vals = ExpressionFactory.evalExpression(tokens);
		return vals;
	}
}