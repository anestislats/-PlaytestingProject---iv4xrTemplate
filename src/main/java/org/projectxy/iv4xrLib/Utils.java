package org.projectxy.iv4xrLib;

import eu.iv4xr.framework.extensions.pathfinding.SimpleNavGraph;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.utils.Pair;
import java.util.List;

import A.B.Monster;


public class Utils {

    /**
     * Convert a 3D coordinate p to a discrete tile-world coordinate. Basically, p
     * will be converted to a pair of integers (p.x,p.y).
     */
    public static Pair<Integer, Integer> toTileCoordinate(Vec3 p) {
        return new Pair((int) p.x, (int) p.y);
    }
    
    public static Vec3 toVec3(int x, int y) {
        return new Vec3(x,y,0) ;
    }
    
    /**
     * Check if two Vec3 represents the same tile coordinate.
     */
    public static boolean sameTile(Vec3 t1, Vec3 t2) {
        Pair<Integer, Integer> p1 = toTileCoordinate(t1) ;
        Pair<Integer, Integer> p2 = toTileCoordinate(t2) ;
        return p1.fst.equals(p2.fst) && p1.snd.equals(p2.snd) ;
    }
    
    public static Integer vec3ToNavgraphIndex(Vec3 p, SimpleNavGraph nav) {
        for(int i=0; i<nav.vertices.size(); i++) {
            if(sameTile(p,nav.vertices.get(i))) return i ;
        }
        return null ;
    }
 

    
    // for debugging
    public static void debugPrintPath(Vec3 agentCurrentPosition, List<Vec3> path) {
        if(path == null) {
            System.out.println(">>> Path to follow is null.") ;
            return ;
        }
        if(path.isEmpty()) {
            System.out.println(">>> Path to follow is empty.") ;
            return ;
        }
        System.out.println(">>> Path to follow, size:" + path.size()) ;
        System.out.println("       agent @" + agentCurrentPosition) ;
        System.out.println("       path[0]: " + path.get(0)) ;
        System.out.println("       last: " + path.get(path.size() - 1)) ;
        int duplicates = 0 ;
        for(int k = 0 ; k<path.size()-1; k++) {
            for (int m = k+1; m<path.size(); m++) {
                var u1 = path.get(k) ;
                var u2 = path.get(m) ;
                if(Utils.sameTile(u1,u2)) {
                    duplicates++ ;
                    System.out.println("         Duplicated: " + u1) ;
                }
            }
        }
        System.out.println("       duplicates: " + duplicates) ;
        if (duplicates>0) throw new Error("The Pathdfinder produces a path containing duplicate nodes!!") ;
    }
    
    
    
    
    public static int itemRestoreAmount (MyAgentState S, String itemId) {
    	
    	WorldEntity currentInv  = S.wom.getElement("Inventory"); 
    	
    	
    	for(WorldEntity item_ : currentInv.elements.values()) {
			if (item_.id == itemId) {
				
				int itemRestoreAmount = item_.getIntProperty("restoreAmount");
				//System.out.println("The selected item has restore amount of "+ itemRestoreAmount);
				
				return itemRestoreAmount;
				
				
			}
			
			
		}
    	return -1;
    	
    	
    }
    
    public static boolean checkHealthRestoreAmount(MyAgentState S, int itemRestoreAmount ) {
    	
    	S.updateState();
    	
    	String agentId = S.wom.agentId ;
        WorldEntity agentCurrentState = S.wom.elements.get(agentId) ;
        WorldEntity agentPreviousState = S.previousWom.elements.get(agentId);
        
        
        int currentHealth = agentCurrentState.getIntProperty("health") ;
        int previousHealth = agentPreviousState.getIntProperty("health") ;
        
		System.out.println("currentHealth "+ currentHealth);
		System.out.println("previousHealth "+ previousHealth);


        
        int healthDifference = Math.abs(currentHealth-previousHealth);
  		
		System.out.println("The health difference between the current and the previous state is: "+ healthDifference);
		
		if (itemRestoreAmount <=0) { // Item restore amount cannot be below or equal to 0
			
			System.out.println("The restore amount of the item is: " + itemRestoreAmount);
			System.out.println();
			return false;
		}
		
		
		if (currentHealth == 10) { 
			System.out.println("The health reached the maximum level after using the health item");
			System.out.println();
			return true;
		}
		
		
		if (healthDifference == itemRestoreAmount) {
			return true;
		}
		else {
			return false;
		}
    	
    	
    }
    
    
    public static int bestWeaponDmg (MyAgentState S, String itemId) {
    	
    	WorldEntity currentInv  = S.wom.getElement("Inventory"); 
    	
    	
    	for(WorldEntity item_ : currentInv.elements.values()) {
			if (item_.id == itemId) {
				
				int bestWeaponDmg = item_.getIntProperty("attackDmg");
				//System.out.println("The selected weapon has "+ bestWeaponDmg +" damage");
				
				return bestWeaponDmg;
				
				
			}
			
			
		}
    	return -1;
    	
    	
    }
    
    public static boolean checkWeaponDmg(int bestWeaponDmg ) {
    	
		System.out.println("The current weapon damage is: "+ bestWeaponDmg);
		
		if (bestWeaponDmg <=0) { // Item weapon damage cannot be below or equal to 0
			System.out.println("The weapon damage cannot be a negative number or equal to 0");
			System.out.println();
			return false;
		}
		else return true;
		
    	
    }
    
    
    public static String monsterId (MyAgentState S, Vec3 monsterLocation ) {
    	
    	for(WorldEntity e : S.wom.elements.values()) {
    		
    		if (e.position == monsterLocation) {
    			return e.id;
    		}
    		
    	}
    	return null;
    	
    }
    
    
    
    
    public static boolean checkDealtDamage (MyAgentState S, String monsterId) {
    	
    	S.updateState();
    	
    	for (WorldEntity e: S.wom.elements.values()) {
	    	
	    	if(e.id == monsterId && S.previousWom != null) {
            	 
        		 
        		 WorldModel current = S.wom ;
        		 WorldModel previous = S.previousWom ;
        		 
        		 WorldEntity monsterCurrentState = current.elements.get(monsterId) ;
	 	         WorldEntity monsterPreviousState = previous.elements.get(monsterId) ;
	 	         
	 	         int currentMonsterLife = monsterCurrentState.getIntProperty("health");
        		 int previousMonsterLife = monsterPreviousState.getIntProperty("health");
        		 
        		 // the player's equipped weapon 
        		 String agentID = S.wom.agentId ;
	    		 
	    		 WorldEntity agentCurrentState = current.elements.get(agentID) ;
	    		 String equippedWeapon = agentCurrentState.getStringProperty("equippedWeaponName");
	    		 int equippedWeaponDmg = agentCurrentState.getIntProperty("equippedWeaponDmg");
	    		 
	    		
        		 int lifeDif = previousMonsterLife - currentMonsterLife;
        		 System.out.println("Agent at position " + S.wom.position + " attacks with "+ equippedWeapon + " for "+ equippedWeaponDmg + " damage");
        		 System.out.println();
        		 
        		
        		 System.out.println("Attack on: monster with id " + e.id + ", at position " + e.position );
        		 System.out.println("Monster's current life: "+ currentMonsterLife);
        		 System.out.println("Monster's previous life: "+ previousMonsterLife);
        		 System.out.println("Player's attack damage: "+ lifeDif);
        		 
        			 
    			 if (currentMonsterLife == 0) {
    				 System.out.println("The monster was killed on this attack");
    				 System.out.println();

    				 return true;

    			 }
    			 
    			 if (lifeDif == equippedWeaponDmg ) {
    				 
    				 System.out.println("The attack damage is equal to the equipped weapon damage");
    				 System.out.println();
    				 
    				 return true;
    				 
    			 }
    			 else {
    				 
    				 System.out.println("There was damage on the monster, but not the correct amount of it");
    				 System.out.println();
    				 
    				 return false;
    			 }
        		
             }
	    	
	    }
    	return false;
    }
    
    
    public static int numberOfNearbyMonsters(MyAgentState S) {
    	
    	//S.updateState();
    	int numberOfMonsters = 0;
    	
    	
    	for (WorldEntity e: S.wom.elements.values()) {
	    	
	    	if(e.type.equals(Monster.class.getSimpleName()) && S.previousWom != null) {
	    		
	    		int monsterXPos = (int) e.position.x;
	    		int monsterYPos = (int) e.position.y;
	    		
	    		int agentXPos = (int) S.wom.position.x;
	    		int agentYPos = (int) S.wom.position.y;
	    		
	    		int dx = (int) Math.abs(agentXPos-monsterXPos) ; // agent-monster distance in x axis
                int dy = (int) Math.abs(agentYPos-monsterYPos) ; // agent-monster distance in y axis
                
                if (dx+dy<=1) {
                	numberOfMonsters++;
                }
	    	}
	    }
    	return numberOfMonsters;
    }
    
    
    
    public static String nearMonsterId(MyAgentState S) {
    	
    	//S.updateState();
    	
    	for (WorldEntity e: S.wom.elements.values()) {
	    	
	    	if(e.type.equals(Monster.class.getSimpleName()) && S.previousWom != null) {
	    		
	    		int monsterXPos = (int) e.position.x;
	    		int monsterYPos = (int) e.position.y;
	    		
	    		int agentXPos = (int) S.wom.position.x;
	    		int agentYPos = (int) S.wom.position.y;
	    		
	    		int dx = (int) Math.abs(agentXPos-monsterXPos) ; // agent-monster distance in x axis
                int dy = (int) Math.abs(agentYPos-monsterYPos) ; // agent-monster distance in y axis
                
                if (dx+dy<=1) {
                	return e.id;
                }
	    	}
	    }
    	return null;
    }
    
    
    
    public static int dxPlusdy(MyAgentState S) {
    	
    	//S.updateState();
    	int diff = 70; //half of the maximum distance in the map 90x50
    	
    	for (WorldEntity e: S.wom.elements.values()) {
	    	
	    	if(e.type.equals(Monster.class.getSimpleName()) && S.previousWom != null) {
	    		
	    		int monsterXPos = (int) e.position.x;
	    		int monsterYPos = (int) e.position.y;
	    		
	    		int agentXPos = (int) S.wom.position.x;
	    		int agentYPos = (int) S.wom.position.y;
	    		
	    		int dx = (int) Math.abs(agentXPos-monsterXPos) ; // agent-monster distance in x axis
                int dy = (int) Math.abs(agentYPos-monsterYPos) ; // agent-monster distance in y axis
                
                if ( dx+dy < diff ) {
                	
                	diff = dx+dy;
                }
                
                //return dx+dy;
	    	}
	    }
    	return diff;
    	
    }
    
    
    
    
    
    public static int monsterAttackDmg(MyAgentState S) {
    	
    	//S.updateState();
    	
    	for (WorldEntity e: S.wom.elements.values()) {
	    	
	    	if(e.type.equals(Monster.class.getSimpleName()) && S.previousWom != null) {
	    		
	    		int eAttackDmg = e.getIntProperty("attackDmg");
	    		return eAttackDmg;
                
	    	}
	    }
    	return -1;
    }
    
    
    public static boolean checkReceivedDmg(MyAgentState S, int numOfNearbyMonsters, int monsterAttackDmg, int dxPlusdy) {
    	
    	//S.updateState();
    	
    	String agentId = S.wom.agentId ;
    	
   	 	WorldModel current = S.wom ;
		WorldModel previous = S.previousWom ;
		 
		WorldEntity agentCurrentState = current.elements.get(agentId) ;
        WorldEntity agentPreviousState = previous.elements.get(agentId) ;
        
        int currentAgentLife = agentCurrentState.getIntProperty("health");
		int previousAgentLife = agentPreviousState.getIntProperty("health");
		
		int movePoints = agentCurrentState.getIntProperty("movingLifePointsLost");
		int previousMovePoints = agentPreviousState.getIntProperty("movingLifePointsLost");
		
		boolean playerTurn = agentCurrentState.getBooleanProperty("playerTurn");
		
		int lifeDif;
		
		int steps = (int) S.wom.timestamp;
		
		
		if ( dxPlusdy<=1 && steps%8==0 ) {
		
			lifeDif = previousAgentLife - (currentAgentLife + 1) ;
		}
		else {
			
			lifeDif = previousAgentLife - currentAgentLife ;
		}
		
		int totalMonsterDmg = monsterAttackDmg*numOfNearbyMonsters;
		
		
		System.out.println("movepoints: " + movePoints);
		System.out.println("steps: " + steps);
		System.out.println("dxPlusdy: " + dxPlusdy);
		

		System.out.println("previousAgentLife: " + previousAgentLife);
		System.out.println("currentAgentLife: " + currentAgentLife);
		System.out.println("lifeDif: " + lifeDif);
		
		System.out.println("numOfNearbyMonsters: " + numOfNearbyMonsters);
		System.out.println("totalMonsterDmg: " + totalMonsterDmg);
		
		System.out.println("monsterAttackDmg: " + monsterAttackDmg);
		
		
		
		
		
		if (lifeDif == totalMonsterDmg) {
			
			System.out.println("The damage of the monsters was dealt correctly on the agent");
			System.out.println();
			
			return true;
			
		}
		else {
			
			System.out.println("The damage of the monsters is wrong");
			System.out.println();
			
			return false;
			
			
		}
    	
    	
    	
    }
    	
    	
    
    
     
    
    
}
