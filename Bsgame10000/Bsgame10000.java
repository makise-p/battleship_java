// T315036 近藤拓弥

import java.util.ArrayList;
import java.util.List;


public class Bsgame10000{

	public static void main(String[] args){
		//int sum = 0;
		//int collect = 0; //logで表示が見れるようにするために表示行数を減らす処理用
		//int batting = 0;//先攻後攻のどちらが多く勝つか見る
		//int front; //先攻後攻対比で使用
		//int much = 0; //分母
		for(int i = 0; i < 10000; i++){
			Game game = new Game();
			game.start(); //普通に動かすとき使用
			/*front = game.start(); //先攻後攻で情報を対比したいときのデータ取りに使用
			if(front != 0){
				much += 1;
			}
			sum += front;//最初から終わりまで*/
			//sum += game.start();
			/*collect += 1;
			if(collect == 50){ //行数減らし
				System.out.println();
				collect = 0;
			}*/
			//batting += game.start();
		}
		//System.out.println();
		//System.out.println("合計" + sum + "ターン");
		//System.out.println("平均" + (double)sum/10000 + "ターン");
		//System.out.println(batting);
	}
}

class Game{
	public int width;//盤面の幅
	public int height;//盤面の高さ
	
	public int turn;//ターン管理
	public int counter;//turn置き換え用
	public int p = 0;
	
	public Player[] players;//Player配列型のplayers
	
	public Game(){
		players = new Player[2];
		
		turn = 0;
		width = height = 5;
		
		players[0] = new FriendPlayer(this);//thisはgame
		players[1] = new EnemyPlayer(this);//ここをSelfControlPlayerにすると自分で操作できる
	}
	
	public int start() {
		boolean finishFlag = false;//決着がついたかどうか、決着がついたらtrueにする
		
		counter = 0;
		while(!finishFlag){
			turn += 1;
			for(int i = 0; i < players.length; i++){//playersの順番（交互）にアクセスする
				players[i].think();
				
				for(int j = 0; j < players.length; j++){//プレイヤーの数
					if(players[j].isDeadAll()){
						finishFlag = true;//ここで終了
						if(j == 0){
							//System.out.println("だだん！後攻の勝利");
							p += 1;
						}else{
							//System.out.println("ででん！先攻の勝利");
							p -= 1;
						}
						break;
					}
				}
				
				if(finishFlag)break;
				counter = turn;
			}
		}
		
		if(p == 1){
			System.out.print(counter + " ");
			return counter;
		}else{
			return 0;
		}
		//return counter; //終了ターンの平均をmain内で出す用
		//System.out.println(p); //pの値が変化しているかここで確認
		//return p; //先攻後攻どちらが勝利が多いかプラマイでmainで判断
	}
		
	public void receiveNotification(Player player, AttackShipNotification notification){//誰の艦から攻撃があったか通知
		for(int i = 0; i < players.length; i++){
			if(players[i] != player){
				players[i].receiveNotification(player, notification);
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
	
	public void receiveNotification(Player player, AttackShipNotification notification){
		boolean splashFlag = false;
		for(int i = 0; i < ships.size(); i++){
			Ship ship = ships.get(i);
			Point difference = new Point(ship.point.x - notification.point.x, ship.point.y - notification.point.y);//攻撃があった地点と自艦の座標の距離
			if(-1 <= difference.x && difference.x <= 1 && -1 <= difference.y && difference.y <= 1){//攻撃が自艦の周りの範囲にあるか判定
				splashFlag = true;
				if(difference.x == 0 && difference.y == 0){
					if(ship.damage()){
						int removeIndex = ships.indexOf(ship);//自分の艦へのダメージを無効化
						ships.remove(removeIndex);
						i -= 1;
					}
				}
			}
		}
		if(splashFlag)
			receiveAttackNotification(player, notification);
	}
	
	public boolean isDeadAll(){
		if(ships.size() == 0)//残存艦が0だった場合
			return true;//全滅してる
		else
			return false;
	}
    
    abstract public void receiveAttackNotification(Player player, AttackShipNotification notification);
}

class FriendPlayer extends Player{//継承したクラスと同じ扱いができる、FriendPlayerはPlayerクラスにキャストできる
    public FriendPlayer(Game _game){
        super(_game);
        
        ships = new ArrayList<Ship>();
        
        Ship Warship = new Ship("戦", rndPoint(game.width, game.height), 3, this);
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
        int targetIndex = (int)(Math.random() * ships.size());//艦の残存数 //動かす艦をランダムに選ぶ
		Ship ship = ships.get(targetIndex);//shipsの配列からランダムに選ばれたのを取りだす
		if(Math.random() < 0.5){
			//移動
			List<Point> pointList = ship.getMoveRange(game.width, game.height, ships);//いけないところは省かれた移動可能範囲
			Point movePoint = pointList.get((int)(Math.random() * pointList.size()));//行動可能範囲からランダムに移動
			ship.move(movePoint);
		}else{
			//攻撃
			List<Point> pointList = ship.getAttackRange(game.width, game.height);//攻撃可能範囲が入ったリスト作成
			Point attackPoint = pointList.get((int)(Math.random() * pointList.size()));
			ship.attack(attackPoint);
		}
    }
    
    public void receiveAttackNotification(Player player, AttackShipNotification notification){
    	//水しぶき(攻撃のあった座標)splashflagを取得できる
    }
}

class EnemyPlayer extends FriendPlayer{
    public EnemyPlayer(Game _game){
        super(_game);
        
        ships = new ArrayList<Ship>();
        
        Ship Warship = new Ship("Ｗ", rndPoint(game.width, game.height), 3, this);
        ships.add(Warship);
        
        Ship ship1 = ships.get(0);
        List<Point>pointList1 = ship1.getSetRange1(game.width, game.height, ships);
        Ship Cruiser = new Ship("Ｃ", pointList1.get((int)(Math.random() * pointList1.size())), 2, this);
        ships.add(Cruiser);
        
        Ship ship2 = ships.get(1);
        List<Point>pointList2 = ship2.getSetRange2(game.width,game.height, ships);
        Ship Submarine = new Ship("Ｓ", pointList2.get((int)(Math.random() * pointList2.size())), 1, this);
		ships.add(Submarine);
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
        List<Point>pointList2 = ship2.getSetRange2(game.width,game.height, ships);
        Ship Submarine = new Ship("Ｓ", pointList2.get((int)(Math.random() * pointList2.size())), 1, this);
		ships.add(Submarine);

    }
    
    public void think(){
        
    }
    public void receiveAttackNotification(Player player, AttackShipNotification notification){
        
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
		AttackShipNotification notification = new AttackShipNotification(name, _point);
		player.game.receiveNotification(player, notification);
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
	
	public boolean damage(){//ダメージを受けたか判定
		hitpoint -= 1;
		if(hitpoint > 0){
			return false;//生きていたら
		}else{
			//System.out.println(name + "撃沈");
			return true;
		}
	}
}

class Notification{//通知の情報の塊

}

class MoveShipNotification extends Notification{
    public String name;
    public Point point;//PointはPointクラスってこと
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