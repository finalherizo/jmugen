package org.lee.mugen.sprite.background;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.lee.mugen.core.StateMachine;
import org.lee.mugen.core.physics.PhysicsEngime;
import org.lee.mugen.parser.air.AirGroup;
import org.lee.mugen.parser.air.AirParser;
import org.lee.mugen.parser.type.StringValueable;
import org.lee.mugen.parser.type.Valueable;
import org.lee.mugen.sff.SffReader;
import org.lee.mugen.sprite.background.BG.Type;
import org.lee.mugen.sprite.base.AbstractAnimManager;
import org.lee.mugen.sprite.baseForParse.SpriteSFF;
import org.lee.mugen.sprite.character.AnimGroup;
import org.lee.mugen.sprite.character.Sprite;
import org.lee.mugen.sprite.character.SpriteHelper;
import org.lee.mugen.sprite.parser.ExpressionFactory;
import org.lee.mugen.sprite.parser.Parser;
import org.lee.mugen.sprite.parser.Parser.GroupText;
import org.lee.mugen.util.BeanTools;

public class Stage {
	public static void main(String[] args) {
		Pattern p = Pattern.compile(_BG_SECTION_REGEX);
		Matcher m = p.matcher("bg sky");
		if (m.find()) {
			System.out.println(m.group(1));
		}
	}
	private Info info = new Info(this);
	private Camera camera = new Camera(this);
	private PlayerInfo playerinfo = new PlayerInfo(this);
	private Scaling scaling = new Scaling(this);
	private Bound bound = new Bound(this);
	private StageInfo stageinfo = new StageInfo(this);
	private Shadow shadow = new Shadow(this);
	private Reflection reflection = new Reflection(this);
	private Music music = new Music(this);
	private BGdef bgdef = new BGdef(this);
	private List<BG> bgs = new ArrayList<BG>();
	private Map<Integer, ArrayList<BG>> bgsMap = new HashMap<Integer, ArrayList<BG>>();
	
	private Map<Integer, BGCtrlDef> bgCtrlDefMap = new HashMap<Integer, BGCtrlDef>();
	
	private SpriteSFF spriteSFF;
	
	private HashMap<Integer, AnimGroup> airGroupMap = new HashMap<Integer, AnimGroup>();

	
	private static AirGroup parseGroup(int actionno, String group) throws IOException {
    	StringTokenizer strToken = new StringTokenizer(group, "\r\n");
    	AirGroup airGrp = new AirGroup();
    	airGrp.action = actionno;
    	//
    	while (strToken.hasMoreTokens()) {
    		String line = strToken.nextToken();
           
            if (Pattern.matches(AirParser._COMMENT_OR_EMPTY_REGEX, line)) {
            	
            } else if (Pattern.matches(AirParser._LOOP_START_REGEX, line)) {
            	AirParser.parseLoopStart(line, airGrp);
    		} else if (AirParser._AIR_DATA_PATTERN.matcher(line).find()) {
    			AirParser.parseAirData(
    						line, 
    						airGrp, 
    						null, 
    						null);
    		}
        }
    	return airGrp;
	}
	

	private static final String _END = "(?:(?: *;.*$)|(?: *$))";

	private static final String _BGCTRL_SECTION_REGEX = " *bgctrl +" + "([a-zA-Z0-9\\.\\ \\-\\_]*) *";
	private static final String _BGCTRLDEF_SECTION_REGEX = " *bgctrldef +" + "(.*) *";
	private static final String _BG_SECTION_REGEX = " *bg +" + "(.*)\\s*";
    public static final String _GRP_ACTION_REGEX = " *begin action +(\\d*) *";

	
	public static Stage buildStage(String filename) throws Exception {
		Stage stage = new Stage();
		String text = IOUtils.toString(new FileInputStream(filename));
		
		List<GroupText> groups = Parser.getGroupTextMap(text);
		BGCtrlDef parentBGCtrlDef = null;
		for (GroupText grp: groups) {
			Object bean = stage;
			String parent = grp.getSection();
			if (Pattern.matches(_GRP_ACTION_REGEX, grp.getSection())) {
				Pattern bgPattern = Pattern.compile(_GRP_ACTION_REGEX);
				Matcher m = bgPattern.matcher(grp.getSection());
				m.find();
				int actionno = Integer.parseInt(m.group(1));
				
				AirGroup ag = parseGroup(actionno, grp.getText().toString());
				stage.getAirGroupMap().put(actionno, new AnimGroup(ag));
				continue;
			} else if (Pattern.matches(_BG_SECTION_REGEX, grp.getSection()) || "bg".equals(grp.getSection())) {
				String bgName;
				if (!"bg".equals(grp.getSection())) {
					Pattern bgPattern = Pattern.compile(_BG_SECTION_REGEX);
					Matcher m = bgPattern.matcher(grp.getSection());
					m.find();
					bgName = m.group(1);
				} else {
					bgName = "";
				}

				
				BG bg = new BG(stage);
				AbstractAnimManager animManager = new AbstractAnimManager(stage.getAirGroupMap());
				bg.setAnimManager(animManager);

				bg.setName(bgName);
				stage.getBgs().add(bg);
				bean = bg;
				parent = "";
			} else if (Pattern.matches(_BGCTRLDEF_SECTION_REGEX, grp.getSection())) {

				parentBGCtrlDef = BGCtrlDef.parseBGCtrlDef(grp.getSection(), grp);
				bean = parentBGCtrlDef;
				stage.getBgCtrlDefMap().put(parentBGCtrlDef.getCtrlid(), parentBGCtrlDef);
				continue;
			} else if (Pattern.matches(_BGCTRL_SECTION_REGEX, grp.getSection())) {
				BGCtrlDef.BGCtrl ctrl = BGCtrlDef.BGCtrl.parseBGCtrl(parentBGCtrlDef, parentBGCtrlDef.getId(), grp.getSection(), grp);
				continue;
			}
			for (String key: grp.getKeyValues().keySet()) {
				final String value = grp.getKeyValues().get(key);

				Object[] objectValues = null;
				

				if (value == null || value.trim().length() == 0) {
					objectValues = new Object[] {null};
				} else {
					Valueable[] values = null;
					if ("bgmusic".equals(key) || "spr".equals(key) || "type".equals(key)) {
						values = new Valueable[] {new StringValueable(value)};
					} else {
						String[] tokens = ExpressionFactory.expression2Tokens(value);
						values = ExpressionFactory.evalExpression(tokens, false, true);
					}
					objectValues = new Object[values.length];
					if ("delta".equals(key)) {
						objectValues = new Object[2];
						objectValues[0] = 1;
						objectValues[1] = 1;
					}
					for (int i = 0; i < values.length; ++i) {
						objectValues[i] = values[i].getValue(null);
					}
				}
				

				try {
					if (objectValues.length == 1) {
						BeanTools.setObject(bean, (parent == null || parent.trim().length() == 0? "": parent + ".") + key, objectValues[0]);
					} else if (objectValues.length > 1) {
						BeanTools.setObject(bean, (parent == null || parent.trim().length() == 0? "": parent + ".") + key, objectValues);
						
					}
				} catch (Exception e) {
					e.printStackTrace();
//					throw new IllegalStateException("setProperties doesn't cause problem ! >" + (parent == null || parent.trim().length() == 0? "": parent + ".") + key + " = " + objectValues);
				}
			}
			if (bean != null && bean instanceof BG) {
				BG bg = (BG) bean;
				if (((BG)bean).getId() != null) {
					ArrayList<BG> list = stage.getBgsMap().get(bg.getId());
					if (list == null) {
						list = new ArrayList<BG>();
						stage.getBgsMap().put(bg.getId(), list);
					}
					bg.setOrder(list.size());
					list.add(bg);
				}
				if (((BG)bean).getType() == Type.ANIM) {
					((BG)bean).getAnimManager().setAction(((BG)bean).getActionno());
				}
				((BG)bean).init();
			}
		}
		File parent = new File(filename).getParentFile();
		File file = new File(parent, stage.getBgdef().getSpr());
		if (!file.exists()) {
			file = new File(parent.getParent(), stage.getBgdef().getSpr());
		}
		
		SffReader sffReader = new SffReader(file.getAbsolutePath(), null);
		SpriteSFF spriteSFF = new SpriteSFF(sffReader, true);
		stage.setSpriteSFF(spriteSFF);
		return stage;
	}
	
	
	
	//////////////////////////////////////
	
	public BGdef getBgdef() {
		return bgdef;
	}
	public void setBgdef(BGdef bgdef) {
		this.bgdef = bgdef;
	}
	public List<BG> getBgs() {
		return bgs;
	}
	public void setBgs(List<BG> bgs) {
		this.bgs = bgs;
	}
	public Bound getBound() {
		return bound;
	}
	public void setBound(Bound bound) {
		this.bound = bound;
	}
	public Camera getCamera() {
		return camera;
	}
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	public Info getInfo() {
		return info;
	}
	public void setInfo(Info info) {
		this.info = info;
	}
	public Music getMusic() {
		return music;
	}
	public void setMusic(Music music) {
		this.music = music;
	}
	public PlayerInfo getPlayerinfo() {
		return playerinfo;
	}
	public void setPlayerinfo(PlayerInfo playerinfo) {
		this.playerinfo = playerinfo;
	}
	public Reflection getReflection() {
		return reflection;
	}
	public void setReflection(Reflection reflection) {
		this.reflection = reflection;
	}
	public Scaling getScaling() {
		return scaling;
	}
	public void setScaling(Scaling scaling) {
		this.scaling = scaling;
	}
	public Shadow getShadow() {
		return shadow;
	}
	public void setShadow(Shadow shadow) {
		this.shadow = shadow;
	}
	public StageInfo getStageinfo() {
		return stageinfo;
	}
	public void setStageinfo(StageInfo stageinfo) {
		this.stageinfo = stageinfo;
	}

	public SpriteSFF getSpriteSFF() {
		return spriteSFF;
	}

	public void setSpriteSFF(SpriteSFF spriteSFF) {
		this.spriteSFF = spriteSFF;
	}

	public HashMap<Integer, AnimGroup> getAirGroupMap() {
		return airGroupMap;
	}



	private int[] getMinMaxForXSprite() {
		int[] minMax = new int[2];
		for (Sprite s: StateMachine.getInstance().getSprites()) {
			if (s instanceof SpriteHelper && (((SpriteHelper)s).getHelperSub().getHelpertype().equals("normal")))
				continue;
			int[] frontBack = s.getInfo().getCurrentFrontAndBackSize();
			int back = s.isFlip()? frontBack[1]: -frontBack[1];
			back = 0;
			minMax[0] = (int) Math.min(s.getInfo().getXPos() + back, minMax[0]);
			minMax[1] = (int) Math.max(s.getInfo().getXPos() + back, minMax[1]);
		}
		return minMax;
	}
	
	private int[] getMinMaxForYSprite() {
		int[] minMax = new int[2];
		for (Sprite s: StateMachine.getInstance().getSprites()) {
			if (s instanceof SpriteHelper && (((SpriteHelper)s).getHelperSub().getHelpertype().equals("normal")))
				continue;
			minMax[0] = (int) Math.min(s.getInfo().getYPos(), minMax[0]);
			minMax[1] = (int) Math.max(s.getInfo().getYPos(), minMax[1]);
		}
		return minMax;
	}



	public void process() {
		for (BG bg: getBgs()) {
			
			if (bg.getId() != null && getBgCtrlDefMap().get(bg.getId()) != null) {
				getBgCtrlDefMap().get(bg.getId()).process();
			} else {
				bg.process();
			}
		}
		int xCam = getCamera().getX();
		int xSpr = 0;

		int[] minMaxX = getMinMaxForXSprite();
		int minX = minMaxX[0];
		int maxX = minMaxX[1];
		xSpr = minX + (maxX - minX)/2;
		
//		int yCam = getCamera().getY();
//		int ySpr = 0;
//
//		int[] minMaxY = getMinMaxForYSprite();
//		int minY = minMaxY[0];
//		int maxY = minMaxY[1];
//		ySpr = minY + (maxY - minY)/2;

		if (getCamera().getEnvShake().getTime() > 0) {
			
		} else {
			int left = getBound().getScreenleft();
			int right = getBound().getScreenright();

			int leftLimit = left + getCamera().getBoundleft()
					- getCamera().getWidth() / 2
					+ getCamera().getTension();

			int rightLimit = -right + getCamera().getBoundright()
					+ getCamera().getWidth() / 2
					- getCamera().getTension();
			
			int diff = xCam + xSpr;
			Sprite sprLeft = StateMachine.getInstance().getSpriteInstance("1");
			Sprite sprRight = StateMachine.getInstance().getSpriteInstance("1");
			
			for (Sprite s: StateMachine.getInstance().getSprites()) {
				if (s instanceof SpriteHelper && (((SpriteHelper)s).getHelperSub().getHelpertype().equals("normal")))
					continue;
				if (sprLeft.getInfo().getXPos() > s.getInfo().getXPos())
					sprLeft = s;
				if (sprRight.getInfo().getXPos() < s.getInfo().getXPos())
					sprRight = s;
				
			}
			
			if (diff < -5 && !PhysicsEngime.isOutOfScreeen(sprRight, 1))
				getCamera().addX(1);
			
			if (diff > 5 && !PhysicsEngime.isOutOfScreeen(sprLeft, 1))
				getCamera().addX(-1);
			//////
			int yCam = getCamera().getY();
			int ySpr = 0;
			for (Sprite spr: StateMachine.getInstance().getSprites()) {
				if (!(spr instanceof SpriteHelper)) {
					ySpr = (int) Math.min(ySpr, spr.getInfo().getYPos());
				}
			}
			ySpr = ySpr < -150? ySpr: 0;
			int yDiff = yCam + ySpr;
			if (yDiff < 0)
				getCamera().setY(getCamera().getY() + 1);
			
			if (yDiff > 0)
				getCamera().setY(getCamera().getY() - 1);
			
			if (getCamera().getX() < leftLimit)
				getCamera().setX(leftLimit);
			if (getCamera().getX() > rightLimit)
				getCamera().setX(rightLimit);
			if (getCamera().getY() < getCamera().getBoundlow())
				getCamera().setY(getCamera().getBoundlow());
			if (getCamera().getY() > -getCamera()
					.getBoundhigh())
				getCamera().setY(-getCamera().getBoundhigh());
		
			
		}
		getCamera().getEnvShake().addTime(-1);
//		getCamera().setX(-429);
	}



	public Map<Integer, ArrayList<BG>> getBgsMap() {
		return bgsMap;
	}



	public void setBgsMap(Map<Integer, ArrayList<BG>> bgsMap) {
		this.bgsMap = bgsMap;
	}



	public Map<Integer, BGCtrlDef> getBgCtrlDefMap() {
		return bgCtrlDefMap;
	}



	public void setBgCtrlDefMap(Map<Integer, BGCtrlDef> bgCtrlDefMap) {
		this.bgCtrlDefMap = bgCtrlDefMap;
	}



	
	
}
