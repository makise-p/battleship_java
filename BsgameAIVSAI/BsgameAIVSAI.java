// T315036 近藤拓弥

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BsgameAIVSAI{
	public static void main(String[] args){
		Game game = new Game();
		game.start();//最初から終わりまで

		InputStreamReader isr = new InputStreamReader(System.in);//改行でシステムを終了させる
	    BufferedReader br = new BufferedReader(isr);
	    try{
	    	String buf = br.readLine();
	    }catch(Exception e){
	 
	    }
	}
}

class Game{
	public int width;//盤面の幅
	public int height;//盤面の高さ
	
	public int turn;//ターン管理
	
	public Player[] players;//Player配列型のplayers
	
	public Game(){
		players = new Player[2];
		
		turn = 0;
		width = height = 5;
		
		players[1] = new FriendPlayer(this);//thisはgame
		players[0] = new EnemyPlayer(this);//ここをSelfControlPlayerにすると自分で操作できる
	}
	
	public void start() {
		System.out.println("初期位置--------------------");
		boolean finishFlag = false;//決着がついたかどうか、決着がついたらtrueにする
		
		drawField();
		
		while(!finishFlag){
			turn += 1;
			System.out.println(turn + "ターン目------------------");
			for(int i = 0; i < players.length; i++){//playersの順番（交互）にアクセスする
				players[i].think();
				drawField();
				
				for(int j = 0; j < players.length; j++){//プレイヤーの数
					if(players[j].isDeadAll()){
						finishFlag = true;//ここで終了
						if(j == 1){
							System.out.println("先攻ＡＩの勝利！！！");
						}else if(j == 0){
							System.out.println("後攻ＡＩの勝利！！！");
						}
						break;
					}
				}
				
				if(finishFlag)break;
				
				InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader br = new BufferedReader(isr);
                try{
                	String buf = br.readLine();
                }catch(Exception e){

                }
			}
		}
	}
	
	public void drawField() {
		String[][] draw = new String[width][height];//描画のためだけの配列
		
		for(int i = 0; i < draw.length; i++)
			Arrays.fill(draw[i], "　");
		
		for(int i = 0; i < players.length; i++){
			for(int j = 0; j < players[i].ships.size(); j++){
				Ship ship = players[i].ships.get(j);
				
				
				/*if(j == 1){
					while(players[i].ships.get(0) == players[i].ships.get(1)){
						ship.point.x = (int)(Math.random() * draw.length);
						ship.point.y = (int)(Math.random() * draw.length);
					}
				}
				if(j == 2){
					if(players[i].ships.get(1) == players[i].ships.get(2) || players[i].ships.get(0) == players[i].ships.get(2)){
						int place = 0;
						while(place == 0){
							ship.point.x = (int)(Math.random() * draw.length);
							ship.point.y = (int)(Math.random() * draw.length);
							if(players[i].ships.get(1) != ship && players[i].ships.get(0) != ship){
								place = 1;
							}
						}
					}
				}*/
				
				draw[ship.point.y][ship.point.x] = ship.name;
			}
		}
		
		System.out.println("　　　 １ ２ ３ ４ ５ ");
		System.out.println("---------------------");
		
		int m,n;
		for(m=0; m<5; m++){
			/*System.out.printf("　 " + (i+1) + " ");
			System.out.printf("|");*///これいかんっぽい
			System.out.printf((m+1) + " ");
			System.out.printf("　　|");
			for(n=0; n<5; n++){
				System.out.printf(draw[m][n]);
				System.out.printf("|");
			}
			System.out.println("");
		}
		System.out.println("---------------------\n");
	}
	
	
	public void receiveNotification(Player player, AttackShipNotification a_notification, MoveShipNotification m_notification){//誰の艦から攻撃があったか、移動があったか通知
		for(int i = 0; i < players.length; i++){
			if(players[i] != player){//ほかのプレイヤーに情報を渡す
				players[i].receiveNotification(player, a_notification, m_notification);
			}
		}
	}
}

abstract class Player{ 	
    public Game game;
    //public Ship[] ships;//Ship配列型のships
	public List<Ship> ships;
    
    public Player(Game _game){
        game = _game;
    }
    
    protected Point rndPoint(int maxW, int maxH){//自身のクラスかサブクラスでしか呼び出せない
        Point point = new Point((int)(Math.random() * maxW), (int)(Math.random() * maxH));
        return point;
    }
    
    abstract public void think();
	
	public void receiveNotification(Player player, AttackShipNotification a_notification, MoveShipNotification m_notification){
		boolean splashFlag = false;
		for(int i = 0; i < ships.size(); i++){
			Ship ship = ships.get(i);
			Point difference = new Point(ship.point.x - a_notification.point.x, ship.point.y - a_notification.point.y);//攻撃があった地点と自艦の座標の距離
			if(-1 <= difference.x && difference.x <= 1 && -1 <= difference.y && difference.y <= 1){//攻撃が自艦の周りの範囲にあるか判定
				splashFlag = true;
				if(difference.x == 0 && difference.y == 0){
					if(ship.damage(a_notification)){//命中か撃沈か判断
						int removeIndex = ships.indexOf(ship);//自分の艦へのダメージを無効化
						ships.remove(removeIndex);
						i -= 1;//？
					}
				}
			}
		}
		if(splashFlag){ 
			receiveAttackNotification(player, a_notification);
			
		}
		/*
		receiveMoveNotification(player, m_notification);
		boolean moveFlag = false;
		for(int i = 0; i < ships.size(); i++){
			Ship ship = ships.get(i);
			Point difference = new Point(ship.point.x - m_notification.point.x, ship.point.y - m_notification.point.y);//移動があった地点と
			if(difference.x != 0 || difference.y != 0 ){
				moveFlag = true;
			}
		}
		if(moveFlag){
			receiveMoveNotification(player, m_notification);
		}
		*/
	}
	
	public boolean isDeadAll(){
		if(ships.size() == 0)//残存艦が0だった場合
			return true;//全滅してる
		else
			return false;
	}
    
    abstract public void receiveAttackNotification(Player player, AttackShipNotification a_notification);
    abstract public void receiveMoveNotification(Player player, MoveShipNotification m_notification);
}

class FriendPlayer extends Player{//継承したクラスと同じ扱いができる、FriendPlayerはPlayerクラスにキャストできる
	int majika = 0;
	boolean AttackFlag = false;
	
	Point splash;
	boolean splashed = false;
	Point remember;
	
	boolean breakFlag = false;
	
    public FriendPlayer(Game _game){
        super(_game); 
        ships = new ArrayList<Ship>();
        
        Ship Warship = new Ship("Ｗ", rndPoint(game.width, game.height), 3, this);
        ships.add(Warship);
        
        Ship ship1 = ships.get(0);
        List<Point>pointList1 = ship1.getSetRange1(game.width, game.height, ships);
        Ship Cruiser = new Ship("Ｃ", pointList1.get((int)(Math.random() * pointList1.size())), 2, this);
       	ships.add(Cruiser);
        
        Ship ship2 = ships.get(1);
        List<Point>pointList2 = ship2.getSetRange2(game.width, game.height, ships);
        Ship Submarine = new Ship("Ｓ", pointList2.get((int)(Math.random() * pointList2.size())), 1, this);
        ships.add(Submarine);
    }
    
    
    public void think(){
        
		if(remember == splash){
			splashed = false;
		}
		if(splashed){
			Ship attacker = ships.get(0);//適当な艦を入れる
			// splash付近の艦をサーチshipに代入
			for(int i=0;i<ships.size();i++){
				Ship ship = ships.get(i);
				if(Math.abs(ship.point.x-splash.x) <= 1&&Math.abs(ship.point.y-splash.y) <= 1){
					attacker = ship;
					break;
				}
			}
			AttackFlag = true;
			//攻撃できる範囲で自艦がいれば攻撃
			attacker.attack(splash);
		}
		//攻撃された地点の周囲に自艦がいないときほかの場所の自艦が攻撃と、最初の1回
		else {
	    	int choose = (int)(Math.random() * 10);
			if(choose >= 1){
				//適当に攻撃
				Ship ship = ships.get(0);
				List<Point> pointList = ship.getAttackRange(game.width, game.height);//攻撃可能範囲が入ったリスト作成
				Point attackPoint = pointList.get((int)(Math.random() * pointList.size()));
				ship.attack(attackPoint);
				
			}else{
				int targetIndex = (int)(Math.random() * ships.size());//艦の残存数 //動かす艦をランダムに選ぶ
				Ship ship = ships.get(targetIndex);//shipsの配列からランダムに選ばれたのを取りだす
				//移動
				List<Point> pointList = ship.getMoveRange(game.width, game.height, ships);//いけないところは省かれた移動可能範囲
				Point movePoint = pointList.get((int)(Math.random() * pointList.size()));//行動可能範囲からランダムに移動
				ship.move(movePoint);
	
			}
		}
    }
//攻撃があった場合→反応して攻撃AI部分
	public void receiveAttackNotification(Player player, AttackShipNotification a_notification){//ここに攻撃を入れると、相手の攻撃がない場合は一切行動しなくなる
		//水しぶき(攻撃のあった座標)を取得できる
		System.out.println("先攻ＡＩの攻撃！！");
		System.out.println("水しぶきあり");
		System.out.println(a_notification.name + "が(" + (a_notification.point.y+1) + "," + (a_notification.point.x+1) + ")の近くにいるようだ！");
	
		remember = splash;
		splash = a_notification.point;
		splashed = true;
	}


    public void receiveMoveNotification(Player player, MoveShipNotification m_notification){
    	
    }
    	
        /*
    	int targetIndex = (int)(Math.random() * ships.size());//艦の残存数 //動かす艦をランダムに選ぶ
		Ship ship = ships.get(targetIndex);//shipsの配列からランダムに選ばれたのを取りだす
		if(Math.random() < 0.5){
			//移動
			List<Point> pointList = ship.getMoveRange(game.width, game.height, ships);//いけないところは省かれた移動可能範囲
			Point movePoint = pointList.get((int)(Math.random() * pointList.size()));//行動可能範囲からランダムに移動
			ship.move(movePoint);
			
			Point difference = new Point((ship.point.x - movePoint.x), (ship.point.y - movePoint.y));
			MoveShipNotification notificaion = new MoveShipNotification(ship.name, difference);
			//返したい
		}else{
			//攻撃
			List<Point> pointList = ship.getAttackRange(game.width, game.height);//攻撃可能範囲が入ったリスト作成
			Point attackPoint = pointList.get((int)(Math.random() * pointList.size()));
			ship.attack(attackPoint);
		}
    }
    
    public void receiveAttackNotification(Player player, AttackShipNotification a_notification){
    	//水しぶき(攻撃のあった座標)を取得できる
    	System.out.println("先攻ＡＩの攻撃！！");
    	System.out.println("水しぶきあり");
    	System.out.println(a_notification.name + "が(" + (a_notification.point.y+1) + "," + (a_notification.point.x+1) + ")の近くにいるようだ！");
    }
    public void receiveMoveNotification(Player player, MoveShipNotification m_notification){
    	
    }*/
}

class EnemyPlayer extends Player{
	int majika = 0;
	boolean AttackFlag = false;
	
	Point splash;
	boolean splashed = false;
	Point remember;
    public EnemyPlayer(Game _game){
        super(_game);
        
        ships = new ArrayList<Ship>();
        
        Ship Warship = new Ship("戦", rndPoint(game.width, game.height), 3, this);//thisはFriendPlayerのアドレスを渡すため、Shipから渡せるようにする

        ships.add(Warship);
        
        Ship ship1 = ships.get(0);
        List<Point>pointList1 = ship1.getSetRange1(game.width, game.height, ships);
        Ship Cruiser = new Ship("巡", pointList1.get((int)(Math.random() * pointList1.size())), 2, this);
        ships.add(Cruiser);
        
        Ship ship2 = ships.get(1);
        List<Point>pointList2 = ship2.getSetRange2(game.width,game.height, ships);
        Ship Submarine = new Ship("潜", pointList2.get((int)(Math.random() * pointList2.size())), 1, this);
		ships.add(Submarine);
    }
	
    public void think(){
    
   		if(remember == splash){
   			splashed = false;
   		}
    	if(splashed){
    		Ship attacker = ships.get(0);//適当な艦を入れる
    		// splash付近の艦をサーチshipに代入
    		for(int i=0;i<ships.size();i++){
    			Ship ship = ships.get(i);
    			if(Math.abs(ship.point.x-splash.x) <= 1&&Math.abs(ship.point.y-splash.y) <= 1){
    				attacker = ship;
    				break;
    			}
    		}
    		
    		
			splashed = false;
    		AttackFlag = true;
    		//攻撃できる範囲で自艦がいれば攻撃
    		attacker.attack(splash);
    	}
    	//攻撃された地点の周囲に自艦がいないときほかの場所の自艦が攻撃と、最初の1回
    	else{
	    	int choose = (int)(Math.random() * 10);
			if(choose >= 1){
				//適当に攻撃
				Ship ship = ships.get(0);
				List<Point> pointList = ship.getAttackRange(game.width, game.height);//攻撃可能範囲が入ったリスト作成
				Point attackPoint = pointList.get((int)(Math.random() * pointList.size()));
				ship.attack(attackPoint);
			}else{
				int targetIndex = (int)(Math.random() * ships.size());//艦の残存数 //動かす艦をランダムに選ぶ
				Ship ship = ships.get(targetIndex);//shipsの配列からランダムに選ばれたのを取りだす
				//移動
				List<Point> pointList = ship.getMoveRange(game.width, game.height, ships);//いけないところは省かれた移動可能範囲
				Point movePoint = pointList.get((int)(Math.random() * pointList.size()));//行動可能範囲からランダムに移動
				ship.move(movePoint);

			}
    	}

    	
    }
	//攻撃があった場合→反応して攻撃AI部分
    public void receiveAttackNotification(Player player, AttackShipNotification a_notification){//ここに攻撃を入れると、相手の攻撃がない場合は一切行動しなくなる
    	//水しぶき(攻撃のあった座標)を取得できる
    	System.out.println("後攻ＡＩの攻撃！！");
    	System.out.println("水しぶきあり");
    	System.out.println(a_notification.name + "が(" + (a_notification.point.y+1) + "," + (a_notification.point.x+1) + ")の近くにいるようだ！");

    	remember = splash;
		splash = a_notification.point;
		splashed = true;
    }


    public void receiveMoveNotification(Player player, MoveShipNotification m_notification){
    	
    }
}

class SelfControlPlayer extends Player{
    public SelfControlPlayer(Game _game){
        super(_game);
        
        ships = new ArrayList<Ship>();
        
        Ship Warship = new Ship("Ｗ", rndPoint(game.width, game.height), 3, this);
        ships.add(Warship);
        
        Ship ship1 = ships.get(0);
        List<Point>pointList1 = ship1.getSetRange1(game.width, game.height, ships);
        Ship Cruiser = new Ship("Ｃ", pointList1.get((int)(Math.random() * pointList1.size())), 2, this);
        ships.add(Cruiser);
        
        Ship ship2 = ships.get(1);
        List<Point>pointList2 = ship2.getSetRange2(game.width, game.height, ships);
        Ship Submarine = new Ship("Ｓ", pointList2.get((int)(Math.random() * pointList2.size())), 1, this);
		ships.add(Submarine);
    }
    
    public void think(){
        
    }
    public void receiveAttackNotification(Player player, AttackShipNotification notification){
        
    }
    public void receiveMoveNotification(Player player, MoveShipNotification m_notification){
    	
    }
}

class Ship{
    public String name;
    public Point point;
    public int hitpoint;
	
	public Player player;
    
    public Ship(String _name, Point _point, int _hitpoint, Player _player){
        name = _name;
        point = _point;
        hitpoint = _hitpoint;
		player = _player;
    }

	public void move(Point _point){
        point = _point;
    }
	
	public List<Point> getSetRange1(int maxW, int maxH, List<Ship> friendShips){
		List<Point> set = new ArrayList<Point>();
		
		for(int i = 0; i < maxH; i++){
			for(int j = 0; j < maxW; j++){
				set.add(new Point(i, j));
			}
		}
		for(int i = 0; i < friendShips.size(); i++){
			Ship set0 = friendShips.get(i);
			int index = set.indexOf(set0.point);
			set.remove(index);
		}
		return set;
	}
	
	public List<Point> getSetRange2(int maxW, int maxH, List<Ship> friendShips){
		List<Point> set = new ArrayList<Point>();
		
		for(int i = 0; i < maxH; i++){
			for(int j = 0; j < maxW; j++){
				set.add(new Point(i, j));
			}
		}
		
		for(int i = 0; i < friendShips.size(); i++){
			Ship temp = friendShips.get(i);
			int index = set.indexOf(temp.point);
			set.remove(index);
		}
		return set;
	}
	
	public List<Point> getMoveRange(int maxW, int maxH, List<Ship> friendShips){//その艦の移動できる範囲をまとめて返す
		List<Point> ret = new ArrayList<Point>();//Point型を指定したリスト
		
		for(int i = 0; i < maxW; i++){
			if(point.x != i){
				ret.add(new Point(i, point.y));//retはreturnで返す変数
			}
		}
		for(int i = 0; i < maxH; i++){
			if(point.y != i){
				ret.add(new Point(point.x, i));
			}
		}
		
		for(int i = 0; i < friendShips.size(); i++){//味方の船をもらう//friendShipsリスト内の数に対してアクセス→座標get
			Ship tmp = friendShips.get(i);
			if(this != tmp){
				int index = ret.indexOf(tmp.point);
				if(index != -1){
					ret.remove(index);//この艦が動けるところからindexと一致したところを消す
				}
			}
		}
		
		return ret;
	}
	
	public void attack(Point _point){
		AttackShipNotification a_notification = new AttackShipNotification(name, _point);
		MoveShipNotification m_notification = new MoveShipNotification(name, _point);
		player.game.receiveNotification(player, a_notification, m_notification);
	}
	
	public List<Point> getAttackRange(int maxW, int maxH){
		List<Point> ret = new ArrayList<Point>();

		for(int i = -1; i < 2; i++){//ここから先は自分の座標のプラスマイナス2の範囲を表すループ
			if(0 < point.x + i && point.x + i < maxW){
				for(int j = -1; j < 2; j++){
					if(0 < point.y + j && point.y + j < maxH){
						ret.add(new Point(point.x + i, point.y + j));
					}
				}
			}
		}
		
		return ret;
	}
	
	public boolean damage(AttackShipNotification notification){//ダメージを受けたか判定
		hitpoint -= 1;
		if(hitpoint > 0){
			System.out.println(notification.name + "からの攻撃が" + name + "に命中");//当たった敵の艦の名前がバレる仕様になってる
			return false;//生きていたら
		}else{
			System.out.println(notification.name + "からの攻撃で" + name + "を撃沈");//ここで艦を撃沈させて周りに敵がいない状態でも水しぶきが発生してるから直す、おらんかったら水しぶき出んようにせんとな
			return true;
		}
	}
}

class Notification{//通知の情報の塊

}

class MoveShipNotification extends Notification{
    public String name;
    public Point point;//PointはPointクラスってこと
    
    public MoveShipNotification(String _name, Point _point){
    	name = _name;
    	point = _point;
    }
}

class AttackShipNotification extends Notification{
	public String name;
    public Point point;
	
	public AttackShipNotification(String _name, Point _point){
		name = _name;
		point = _point;
	}
}

class Point { //座標
    public int x;
    public int y;
    
    public Point(int _x, int _y){
        x = _x;
        y = _y;
    }
	
	public boolean equals(Object obj){//オーバーライドする仕組みすべてのクラスはObjectをオーバーライド。Object型はすべてのクラスの親クラス
		Point t = (Point)obj;
		
		if(t.x == x && t.y == y){
			return true;
		}else{
			return false;
		}
	}
}