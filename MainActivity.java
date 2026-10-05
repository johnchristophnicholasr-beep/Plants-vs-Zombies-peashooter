package com.johncreator.peashooteronly;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class MainActivity extends Activity {
    GameView game;
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        game=new GameView();
        setContentView(game);
    }

    class GameView extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Random rng=new Random();
        int screen=0, world=0, wave=1, lane=2, plantHp=100;
        long last=0;
        String[] worlds={"DAY","NIGHT","POOL","NIGHT POOL","ROOF"};
        String plant="PEASHOOTER";
        ArrayList<Zombie> zombies=new ArrayList<>();
        ArrayList<Pea> peas=new ArrayList<>();
        String buff="NONE";
        int buffDamage=10;
        boolean fog=false, boss=false;

        GameView(){super(MainActivity.this); p.setTypeface(Typeface.create("sans",0));}

        void txt(Canvas c,String s,float x,float y,float size){
            p.setTextSize(size); p.setColor(Color.WHITE); c.drawText(s,x,y,p);
        }
        void box(Canvas c,float l,float t,float r,float b,int color){
            p.setColor(color); c.drawRect(l,t,r,b,p);
        }

        protected void onDraw(Canvas c){
            super.onDraw(c);
            if(screen==0) lobby(c);
            else if(screen==1) tutorial(c);
            else play(c);
            invalidate();
        }

        void lobby(Canvas c){
            box(c,0,0,getWidth(),getHeight(),Color.rgb(35,100,55));
            box(c,0,getHeight()*0.62f,getWidth(),getHeight(),Color.rgb(115,75,40));
            txt(c,"PLANTS VS ZOMBIE",40,70,34);
            txt(c,"PEASHOOTER ONLY",40,112,34);
            txt(c,"2D • SIX LANES • NO ADS",40,145,18);
            box(c,getWidth()/2-150,180,getWidth()/2+150,260,Color.rgb(30,120,60));
            txt(c,"START",getWidth()/2-55,232,32);
            box(c,25,getHeight()-95,310,getHeight()-25,Color.rgb(45,70,120));
            txt(c,"ASSISTANT HELPER",45,getHeight()-52,22);
            txt(c,"Classic backyard-inspired lobby",getWidth()/2-145,getHeight()-45,18);
        }

        void tutorial(Canvas c){
            box(c,0,0,getWidth(),getHeight(),Color.rgb(30,80,50));
            txt(c,"TUTORIAL "+wave+"-1",30,55,30);
            if(wave==1){
                txt(c,"Move your plant between all 6 lanes.",30,95,20);
                txt(c,"Your plant shoots automatically.",30,125,20);
                txt(c,"Zombies can enter any lane.",30,155,20);
            } else {
                txt(c,"BUFFS CHANGE YOUR POWER.",30,95,20);
                txt(c,"Fire • Snow • Obsidian • Damage • HP • Defense",30,125,18);
                txt(c,"Use the lane controls to survive.",30,155,20);
            }
            box(c,getWidth()-210,getHeight()-85,getWidth()-25,getHeight()-25,Color.rgb(40,110,70));
            txt(c,"PLAY",getWidth()-155,getHeight()-45,24);
        }

        void play(Canvas c){
            int w=getWidth(), h=getHeight();
            int sky=world==1||world==3?Color.rgb(25,35,65):Color.rgb(100,180,100);
            box(c,0,0,w,h,sky);
            float top=105, bottom=h-30, laneH=(bottom-top)/6f;
            for(int i=0;i<6;i++){
                box(c,0,top+i*laneH,w,top+(i+1)*laneH,
                        i%2==0?Color.argb(55,255,255,255):Color.argb(30,0,0,0));
            }
            if(fog){
                box(c,0,top,w,bottom,Color.argb(150,25,25,35));
            }
            txt(c,worlds[world]+"  WAVE "+wave+"/10",20,35,24);
            txt(c,plant+"  HP "+plantHp,20,68,18);
            txt(c,"BUFF: "+buff+"  DMG "+buffDamage,330,35,18);
            if(boss) txt(c,"BOSS",w-100,35,22);

            // plant
            float py=top+(lane+.5f)*laneH;
            p.setColor(Color.rgb(50,180,70)); c.drawCircle(90,py,28,p);
            p.setColor(Color.rgb(20,100,40)); c.drawCircle(112,py-3,11,p);

            // peas
            for(Pea q:peas){
                p.setColor(q.fire?Color.rgb(255,120,30):q.snow?Color.CYAN:Color.rgb(80,220,80));
                c.drawCircle(q.x,q.y,7,p);
            }
            // zombies
            for(Zombie z:zombies){
                p.setColor(z.color); c.drawCircle(z.x,z.y,25,p);
                txt(c,z.type,z.x-30,z.y+42,11);
            }
            // controls
            for(int i=0;i<6;i++){
                box(c,10+i*70,h-25,65+i*70,h,Color.rgb(40,90,140));
            }
            txt(c,"1",30,h-7,14); txt(c,"2",100,h-7,14); txt(c,"3",170,h-7,14);
            txt(c,"4",240,h-7,14); txt(c,"5",310,h-7,14); txt(c,"6",380,h-7,14);

            if(System.currentTimeMillis()-last>900){
                spawn();
                shoot();
                update();
                last=System.currentTimeMillis();
            }
        }

        void spawn(){
            if(rng.nextFloat()<.65f){
                String[] types={"Zombie","Conehead","Buckethead","Pole Vaulting","Newspaper","Screen Door","Football","Dancing","Ducky Tube","Snorkel","Dolphin Rider","Zomboni","Jack-in-the-Box","Balloon","Digger","Pogo","Ladder","Catapult","Gargantuar","Imp"};
                String type=types[rng.nextInt(types.length)];
                if(world==0 && rng.nextFloat()<.35) type="Zombie";
                if(world==4 && rng.nextFloat()<.18) type="Zomboni";
                int l=rng.nextInt(6);
                float top=105, laneH=(getHeight()-135)/6f;
                zombies.add(new Zombie(type,getWidth()+40,top+(l+.5f)*laneH));
            }
        }
        void shoot(){
            float top=105,laneH=(getHeight()-135)/6f,y=top+(lane+.5f)*laneH;
            int count=plant.equals("THREEPEATER")?3:1;
            for(int i=0;i<count;i++){
                Pea q=new Pea(120,y);
                q.fire=buff.equals("FIRE PEA")||buff.equals("OBSIDIAN PEA");
                q.snow=buff.equals("SNOW PEA")||buff.equals("OBSIDIAN PEA");
                q.damage=buffDamage;
                peas.add(q);
            }
        }
        void update(){
            for(Pea q:peas) q.x+=45;
            for(Zombie z:zombies) z.x-=7;
            Iterator<Zombie> it=zombies.iterator();
            while(it.hasNext()){
                Zombie z=it.next();
                if(z.x<80){plantHp-=Math.max(1,world+1); it.remove();}
            }
            Iterator<Pea> ip=peas.iterator();
            while(ip.hasNext()){
                Pea q=ip.next();
                for(Zombie z:zombies){
                    if(Math.abs(q.x-z.x)<24 && Math.abs(q.y-z.y)<28){
                        z.hp-=q.damage; ip.remove(); break;
                    }
                }
            }
            Iterator<Zombie> iz=zombies.iterator();
            while(iz.hasNext()) if(iz.next().hp<=0) iz.remove();
            if(plantHp<=0){plantHp=100; zombies.clear(); peas.clear();}
        }

        void startGame(){
            screen=2; world=0; wave=1; lane=2; plantHp=100;
            plant="PEASHOOTER"; buff="NONE"; buffDamage=10; fog=false; boss=false;
        }

        public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP) return true;
            float x=e.getX(),y=e.getY();
            if(screen==0){
                if(y>160 && y<290){screen=1; wave=1;}
                else if(y>getHeight()-115){screen=1; wave=1;}
            } else if(screen==1){
                if(x>getWidth()-240 && y>getHeight()-110){
                    if(wave==1){wave=2;} else startGame();
                }
            } else {
                if(y>getHeight()-55){
                    int l=(int)(x/70);
                    if(l>=0&&l<6) lane=l;
                }
                // long horizontal upper tap changes a random buff for testing/tutorial
                else if(y<100){
                    String[] bs={"FIRE PEA","SNOW PEA","OBSIDIAN PEA","DAMAGE x5","HP +100","DEFENSE +50","RANDOM PICK"};
                    buff=bs[rng.nextInt(bs.length)];
                    if(buff.equals("FIRE PEA"))buffDamage=15;
                    else if(buff.equals("SNOW PEA"))buffDamage=16;
                    else if(buff.equals("OBSIDIAN PEA"))buffDamage=25;
                    else if(buff.equals("DAMAGE x5"))buffDamage=50;
                    else buffDamage=10;
                }
            }
            return true;
        }

        class Zombie{
            String type; float x,y; int hp; int color;
            Zombie(String t,float xx,float yy){type=t;x=xx;y=yy;
                hp=t.equals("Gargantuar")?180:(t.equals("Buckethead")?80:40);
                color=t.equals("Gargantuar")?Color.rgb(100,40,120):Color.rgb(100,150,100);}
        }
        class Pea{
            float x,y; int damage=10; boolean fire,snow;
            Pea(float xx,float yy){x=xx;y=yy;}
        }
    }
}
