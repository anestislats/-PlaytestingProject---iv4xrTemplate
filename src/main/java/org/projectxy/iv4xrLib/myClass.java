package org.projectxy.iv4xrLib;

import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.IFELSE;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.SUCCESS;

import java.io.IOException;

import A.B.Bow;
import A.B.Food;
import A.B.Gold;
import A.B.HealthPotion;
import A.B.Monster;
import A.B.Sword;
import A.B.Water;
import A.B.Weapon;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.mainConcepts.ObservationEvent.ScalarTracingEvent;
import eu.iv4xr.framework.mainConcepts.ObservationEvent.VerdictEvent;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;

public class myClass {

	static boolean reach_the_stairs_until_fifth_level = true;
	static boolean interact_with_everything_and_reach_the_stairs = false;
	
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		// i is our seed numbers
		for (int i = 15440; i < 15451; i++) {
			
			if (reach_the_stairs_until_fifth_level) {
				reach_the_stairs_until_fifth_level(i);
			}
			
			
			if (interact_with_everything_and_reach_the_stairs) {
				interact_with_everything_and_reach_the_stairs(i);
			}
			
		}
		
	}

	
	////////////////////////////////////////
	///////////////////////////////////////
	
	public static void reach_the_stairs_until_fifth_level(int seed) throws InterruptedException, IOException {
		// This goal lets the agent going through levels by reaching the stairs, until it reaches the 5th level (first Boss)
		
		// Measure the execution time until the goal is reached
		long start = System.currentTimeMillis();
		int testCompletedSuccessfully = 0;
		
		
		int monX=-1, monY = -1;
		int healX=-1, healY = -1;
		int weapX=-1, weapY = -1;
		
		
		// launch the game:
		NethackWrapper driver = new NethackWrapper();
		driver.launchNethack(new NethackConfiguration(), seed);  // giving seed number 3 

		// Create an agent, and attaching to it a clean state and environment:
		TestAgent agent = new TestAgent();
		MyAgentState state = new MyAgentState();
		agent.attachState(state);
		MyEnv env = new MyEnv(driver);
		agent.attachEnvironment(env);
		
		
		
		
		// Data collector
		//TestAgent agent = new TestAgent();
		TestDataCollector collector = new TestDataCollector() ;
		state.owner = agent ;
		agent.setTestDataCollector(collector) ;
		
		// String itemId="";
		

		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
		}
		
		
		// give a goal-structure to the agent:
		GoalStructure g = SEQ(
		                      GoalLib.entityVisited_5_level(agent,"Stairs",3)
		                      );

		agent.setGoal(g);

		int turn = 0;
		int numberOfFailingCheck = 0;
		int numberOfPassingCheck = 0;
		
		while (g.getStatus().inProgress()) {
			
		    agent.update();
		    turn++;
		    
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			state.updateState();
			
			String agentId = state.wom.agentId ;
		    
		    WorldModel current = state.wom ;
			WorldModel previous = state.previousWom ;
			 
			WorldEntity agentCurrentState = current.elements.get(agentId) ;
	        WorldEntity agentPreviousState = previous.elements.get(agentId) ;
	        
	        int currentAgentLife = agentCurrentState.getIntProperty("health");
			int previousAgentLife = agentPreviousState.getIntProperty("health");
			
			int numOfNearbyMonsters = Utils.numberOfNearbyMonsters(state);
		    
			if (currentAgentLife<previousAgentLife && numOfNearbyMonsters !=0) {
		    
			    System.out.println("----------------------------MONSTER'S ATTACK DAMAGE------------------------------------") ;
			    
			    System.out.println("-------------------------------" + currentAgentLife) ;
			    System.out.println("-------------------------------" + previousAgentLife) ;


			    boolean correctAmountOfReceivedDmg = false;
			    
			    int dxPlusdy = Utils.dxPlusdy(state);
			    
			    
			    
			    if (numOfNearbyMonsters != 0) {
			    	String nearMonsterId = Utils.nearMonsterId(state);
			    	int monsterAttackDmg = Utils.monsterAttackDmg(state);
			    	correctAmountOfReceivedDmg = Utils.checkReceivedDmg(state, numOfNearbyMonsters, monsterAttackDmg, dxPlusdy );
			    }
			    
			    System.out.println("Was the correct amount of damage received from the monsters?		>>> "+ correctAmountOfReceivedDmg);
		        System.out.println();
		        
		        System.out.println("--------------------------------------------------------------------------------------") ;
		        System.out.println("--------------------------------------------------------------------------------------") ;
			}
			
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			int numberOfNewFails = agent.getTestDataCollector().getNumberOfFailVerdictsSeen() -  numberOfFailingCheck ;
			numberOfFailingCheck += numberOfNewFails ;
			
			int numberOfNewPasses = agent.getTestDataCollector().getNumberOfPassVerdictsSeen() - numberOfPassingCheck;
			numberOfPassingCheck += numberOfNewPasses;
			
			int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
			int health = agentCurrentState.getIntProperty("health");

			ScalarTracingEvent scalarValues = new ScalarTracingEvent(
								new Pair("posx", state.wom.position.x) , 
								new Pair("posz", state.wom.position.y) ,
								
								new Pair("level", currentLevel),
								new Pair("health", health),
								
								new Pair("seconds", (System.currentTimeMillis()-start)/1000F ) , 
								new Pair("steps", state.wom.timestamp ) , 
								
								new Pair("newTests", (collector.getNumberOfPassVerdictsSeen() + collector.getNumberOfFailVerdictsSeen())),
								new Pair("newPasses", numberOfNewPasses),
								new Pair("newFails", numberOfNewFails)
								
								
//								// Collect these values to compare how different are the levels 
//								new Pair("monsterAmount", monsterAmount),
//								new Pair("healthPorAmount", healthPorAmount),
//								new Pair("weaponAmount", weaponAmount)
								
								) ;		

			agent.getTestDataCollector().registerEvent(agent.getId(), scalarValues);
		
			Thread.sleep(50);
			if (!state.isAlive() || turn > 1000) {
				
				// forcing break the agent seems to take forever...
				break;
			}
		}
		
		
		
		System.out.println("** Number of passes: "  + collector.getNumberOfPassVerdictsSeen()) ;
		System.out.println("** Number of violations: "  + collector.getNumberOfFailVerdictsSeen()) ;
		
		//collector.getTestAgentTrace(agent.getId()) ;

		//assertTrue(collector.getNumberOfFailVerdictsSeen() == 0) ;
		collector.saveTestAgentScalarsTraceAsCSV(agent.getId(), "Seed."+seed+".5level.x.y.level.health.seconds.steps.newtests.passes.fails.csv");
	
		////////////////////////////////// Check rest items in inventory ////////////////////////////////////////
		
		Utils.CheckInvItemValues(state);
			
		
		int numberOfNewFails = agent.getTestDataCollector().getNumberOfFailVerdictsSeen() -  numberOfFailingCheck ;
		
		int numberOfNewPasses = agent.getTestDataCollector().getNumberOfPassVerdictsSeen() - numberOfPassingCheck;
		
		
		numberOfPassingCheck += numberOfNewPasses;
		numberOfFailingCheck += numberOfNewFails ;
		
		
		if (g.getStatus().success()) {
			testCompletedSuccessfully = 1;
		}
		else if (g.getStatus().failed()) {
			testCompletedSuccessfully = 0;
		}
		else {
			testCompletedSuccessfully = 2;
		}
		
		
		System.out.println("test Completed Successfully:	>>> "+ testCompletedSuccessfully);		// 0 for fail, 1 for success, 2 otherwise (in progress) 
		
		ScalarTracingEvent scalarValues1 = new ScalarTracingEvent(
				
				new Pair("newTests", (collector.getNumberOfPassVerdictsSeen() + collector.getNumberOfFailVerdictsSeen())),
				new Pair("newPasses", numberOfNewPasses),
				new Pair("newFails", numberOfNewFails),
				new Pair("seed",  seed),
				new Pair("testCompletedSuccessfully", testCompletedSuccessfully)
				
				
				) ;		

		agent.getTestDataCollector().registerEvent(agent.getId(), scalarValues1);
		
	
		System.out.println("** Rest items: Number of passes: "  + collector.getNumberOfPassVerdictsSeen()) ;
		System.out.println("** Rest items: Number of violations: "  + collector.getNumberOfFailVerdictsSeen()) ;
		collector.getTestAgentTrace(agent.getId()) ;

		//assertTrue(collector.getNumberOfFailVerdictsSeen() == 0) ;
		collector.saveTestAgentScalarsTraceAsCSV(agent.getId(), "Seed."+seed+".5level.Testing rest items.csv");
		
		System.out.println(">>> Agent alive:" + state.isAlive());
        System.out.println(">>> Goal status:" + g.getStatus());
         
        // Stop timer and convert to seconds
        long elapsedTimeMillis = System.currentTimeMillis()-start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		System.out.println(">>> Execution Time: " + elapsedTimeSec + "sec");
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void interact_with_everything_and_reach_the_stairs(int seed) throws InterruptedException, IOException {
		
		// Measure the execution time until the goal is reached
		long start = System.currentTimeMillis();
		int testCompletedSuccessfully = 0;
		
		
		// launch the game:
		NethackWrapper driver = new NethackWrapper();
		driver.launchNethack(new NethackConfiguration(),seed);

		// Create an agent, and attaching to it a clean state and environment:
		TestAgent agent = new TestAgent();
		MyAgentState state = new MyAgentState();
		agent.attachState(state);
		MyEnv env = new MyEnv(driver);
		agent.attachEnvironment(env);
		
		//state.updateState();
		
		// data collector
		TestDataCollector collector = new TestDataCollector() ;
		state.owner = agent ;
		agent.setTestDataCollector(collector) ;


		for (WorldEntity e : state.wom.elements.values()) {
			System.out.println(">>> " + e.type + ", id=" + e.id + ", @" + e.position);
			
			if (e.type == "Bow" || e.type == "Sword") {
				
				int dmg = e.getIntProperty("attackDmg");
				
				System.out.println(">>>@ " + e.type + ", damage= " + dmg);
				
			}
			
		}
		
		while (true) {
			
			int minDist = 140; //90+50 max distance
			
			WorldEntity targetEntity = null;
			WorldEntity stairs = state.wom.getElement("Stairs") ;
			for (WorldEntity e: state.wom.elements.values()) {
				//int minDist = 140;
				//System.out.println("closes item is in distance: " + minDist);
				
				if(	(e.type.equals(HealthPotion.class.getSimpleName()) ) ||
	            		 (e.type.equals(Water.class.getSimpleName()) ) ||
	            		 (e.type.equals(Gold.class.getSimpleName()) ) ||
	            		 (e.type.equals(Food.class.getSimpleName()) ) ||
	            		 (e.type.equals(Sword.class.getSimpleName()) ) ||
	            		 (e.type.equals(Bow.class.getSimpleName()) ) ||
	            		 (e.type.equals(Monster.class.getSimpleName() ) )
	            		 )
	            {
					
					
					
				    if(stairs!=null && Utils.sameTile(stairs.position,e.position)) {
                        // not going to test an entity that is ON stairs
                        continue ;
                    }
				    
				    
//					targetEntity = e ;
//					break ;
				     
				     int ix = (int) e.position.x; 					// item's x coordinate
	                 int iy = (int) e.position.y;					// item's y coordinate
	                 int ax = (int) state.wom.position.x;				// agent's x coordinate
	                 int ay = (int) state.wom.position.y;				// agent's y coordinate
	                 
	                 int dx = (int) Math.abs(ax-ix) ; // agent-item distance in x axis
	                 int dy = (int) Math.abs(ay-iy) ; // agent-item distance in y axis
	                 
	                 
	                 if (dx + dy < minDist) {
	                	 
	                	 minDist = dx + dy;
	                
	                	 targetEntity = e ;
	                 }
	            }
				//minDist=140;
				
			}
			
			if (targetEntity == null) break ;
			
			GoalStructure g1;
			final String targetId = targetEntity.id ;
			
			
//			if(targetEntity.type.equals(Bow.class.getSimpleName() ) ||
//				targetEntity.type.equals(Sword.class.getSimpleName() )	){
//				
//				int WeaponDmg = Utils.bestWeaponDmg(state, targetEntity.id);
//				System.out.println("This " + targetEntity.type + targetEntity.id + " weapon has "+ WeaponDmg +" damage");
//				
//				boolean correctAmountOfWeaponDmg = Utils.checkWeaponDmg(WeaponDmg);
//	      		System.out.println("Is the weapon damage a valid amount? 		>>> "+ correctAmountOfWeaponDmg);
//	      		
//	      		
//	      		if(collector != null) {
//					VerdictEvent verdict = new VerdictEvent("Weapon damage",
//							targetEntity.id + targetEntity.type + " has damage of " + WeaponDmg,
//							correctAmountOfWeaponDmg) ;
//					collector.registerEvent(state.owner.getId(), verdict) ;
//				}
//				
//				
//			}
//			
//			
//			if(targetEntity.type.equals(HealthPotion.class.getSimpleName() ) ||
//					targetEntity.type.equals(Food.class.getSimpleName() ) ||
//					targetEntity.type.equals(Water.class.getSimpleName() )	){
//					
//					int restoreAmount = targetEntity.getIntProperty("restoreAmount");
//					
//					System.out.println("This " + targetEntity.type + " item restores "+ restoreAmount +" life points");
//					
//					boolean correctAmountOfRestorePoints = Utils.checkRestoreItemAmount(restoreAmount);
//		      		System.out.println("Does the health item restore a valid amount? 		>>> "+ correctAmountOfRestorePoints);
//		      		
//		      		
//		      		if(collector != null) {
//						VerdictEvent verdict = new VerdictEvent("Restore life points",
//								targetEntity.id + targetEntity.type + " restores " + restoreAmount,
//								correctAmountOfRestorePoints) ;
//						collector.registerEvent(state.owner.getId(), verdict) ;
//					}
//					
//					
//				}
			
			
			
			
			if(targetEntity.type.equals(Monster.class.getSimpleName() ) ) {
				System.out.println("######### Testing monster " + targetId) ;
				g1 = GoalLib.closeToAMonster(agent, targetId, 0) ;
				
			}
			else {
                System.out.println("######### Testing entity " + targetEntity.type + " " +  targetId) ;    
                g1 = FIRSTof(
                        SEQ( GoalLib.entityVisited(agent, targetEntity.id,3),
                             IFELSE((MyAgentState S) -> S.wom.elements.get(targetId) != null,
                                GoalLib.pickUpItem(),
                                SUCCESS())
                            ));
			}
			
			agent.setGoal(g1);
			
			int turn = 0;
			int numberOfFailingCheck = 0 ;
			int numberOfPassingCheck = 0 ;
			
			
			while (g1.getStatus().inProgress()) {
				
				//agent.update();
			    //turn++;
				
				state.updateState();
			    System.out.println(">>> Agent @" + state.wom.position + ", alive:" + state.isAlive() + ", move number: " + state.wom.timestamp) ;
			    
			    //state.updateState();
			    
			    String agentId = state.wom.agentId ;
			    
			    WorldModel current = state.wom ;
				WorldModel previous = state.previousWom ;
				 
				WorldEntity agentCurrentState = current.elements.get(agentId) ;
		        WorldEntity agentPreviousState = previous.elements.get(agentId) ;
		        
		        int currentAgentLife = agentCurrentState.getIntProperty("health");
				int previousAgentLife = agentPreviousState.getIntProperty("health");
				
				int numOfNearbyMonsters = Utils.numberOfNearbyMonsters(state);
			    
				if (currentAgentLife<previousAgentLife && numOfNearbyMonsters !=0) {
			    
				    System.out.println("----------------------------MONSTER'S ATTACK DAMAGE------------------------------------") ;
				    
				    System.out.println("-------------------------------" + currentAgentLife) ;
				    System.out.println("-------------------------------" + previousAgentLife) ;


				    boolean correctAmountOfReceivedDmg = false;
				    
				    int dxPlusdy = Utils.dxPlusdy(state);
				    
				    
				    
				    if (numOfNearbyMonsters != 0) {
				    	String nearMonsterId = Utils.nearMonsterId(state);
				    	int monsterAttackDmg = Utils.monsterAttackDmg(state);
				    	correctAmountOfReceivedDmg = Utils.checkReceivedDmg(state, numOfNearbyMonsters, monsterAttackDmg, dxPlusdy );
				    }
				    
				    //int monsterAttackDmg = Utils.monsterAttackDmg(state);
				    //boolean correctAmountOfReceivedDmg = Utils.checkReceivedDmg(state, numOfNearbyMonsters, monsterAttackDmg );
				    
				    System.out.println("Was the correct amount of damage received from the monsters?		>>> "+ correctAmountOfReceivedDmg);
			        System.out.println();
			        
			        
			        collector = state.owner.getTestDataCollector() ;
					if(collector != null) {
						VerdictEvent verdict = new VerdictEvent("Received damage",
								" The amount of damage received from the monster(s) was the expected ",
								correctAmountOfReceivedDmg) ;
						collector.registerEvent(state.owner.getId(), verdict) ;
					}
			        
			        System.out.println("--------------------------------------------------------------------------------------") ;
			        System.out.println("--------------------------------------------------------------------------------------") ;
				}
			    
				
				int numberOfNewFails = agent.getTestDataCollector().getNumberOfFailVerdictsSeen() -  numberOfFailingCheck ;
				
				int numberOfNewPasses = agent.getTestDataCollector().getNumberOfPassVerdictsSeen() - numberOfPassingCheck;
				
				
				numberOfPassingCheck += numberOfNewPasses;
				numberOfFailingCheck += numberOfNewFails ;
				
				
				int news = numberOfNewFails + numberOfNewPasses ;
				
				int checks = numberOfPassingCheck + numberOfFailingCheck;
				
				
				
				System.out.println("@@@>>> news: " +  news);
				System.out.println("@@@>>> total: " +  (checks - news));
				System.out.println("@@@>>> All Checks: " + checks);
				
				
				int currentLevel = agentCurrentState.getIntProperty("currentLevel") ;
				int health = agentCurrentState.getIntProperty("health");
				
				
//				// //////////////////////// Check all items in inventory ////////////////////////////////////////
//				WorldModel old = state.previousWom ;
//		        
//		        WorldEntity oldInv = old.getElement("Inventory");
//		        WorldEntity currentInv = current.getElement("Inventory");
//		        
//		        int oldInvSize = oldInv.elements.size();
//		        int currentInvSize = currentInv.elements.size();
//		        
//		        
//		        
//		        if (currentInvSize > oldInvSize) {
//		        	
//		        	Utils.CheckInvItemValues(state);
//		        	
//		        }
		        
		        
				
				

				ScalarTracingEvent scalarValues = new ScalarTracingEvent(
									
									new Pair("posx", state.wom.position.x) ,
									new Pair("posz", state.wom.position.y),
									
									new Pair("health", health),
									new Pair("level", currentLevel),
									
									new Pair("seconds", (System.currentTimeMillis()-start)/1000F ) , 
									new Pair("steps", state.wom.timestamp ), 
									
									new Pair("newTests", (collector.getNumberOfPassVerdictsSeen() + collector.getNumberOfFailVerdictsSeen())),
									new Pair("newPasses", numberOfNewPasses),
									new Pair("newFails", numberOfNewFails)
									
									
									) ;		

				agent.getTestDataCollector().registerEvent(agent.getId(), scalarValues);
			  
	    
			   
			    
			    agent.update();
			    turn++;
				
				Thread.sleep(50);
				if (!state.isAlive() || turn > 600) {
					// forcing break the agent seems to take forever...
					break;
				}
			}
			
			
			
			System.out.println("** Number of passes: "  + collector.getNumberOfPassVerdictsSeen()) ;
			System.out.println("** Number of violations: "  + collector.getNumberOfFailVerdictsSeen()) ;
			collector.getTestAgentTrace(agent.getId()) ;

			//assertTrue(collector.getNumberOfFailVerdictsSeen() == 0) ;
			collector.saveTestAgentScalarsTraceAsCSV(agent.getId(), "seed."+seed+".interact.posx.posz.health.level.seconds.steps.newTests.newPasses.newFails.csv");

			
			
			if(!state.isAlive()) {
			    // well... the agent is dead...
			    return ;
			}
		}
		
		
		System.out.println("######### Going to next the stairs") ;    
		
		GoalStructure g2 = GoalLib.entityVisited_all(agent,"Stairs",3);

		agent.setGoal(g2);

		int turn = 0;
		int numberOfFailingCheck = 0 ;
		int numberOfPassingCheck = 0 ;
		
		
		while (g2.getStatus().inProgress()) {
		    agent.update();
		    turn++;
			System.out.println("[" + turn + "] agent@" + state.wom.position);
			Thread.sleep(50);
			if (turn > 500) {
				// forcing break the agent seems to take forever...
				break;
			}
		}

		
		System.out.println(">>> Goal status:" + g2.getStatus());
	
		g2.printGoalStructureStatus();
		
		
		
//		WorldEntity stairs = state.wom.getElement("Stairs");
//		int stairX = (int) stairs.position.x;
//		int stairY = (int) stairs.position.y;
//		
//		int dx = (int) (state.wom.position.x - stairX);
//		int dy = (int) (state.wom.position.y - stairY);
		
		
			
		Utils.CheckInvItemValues(state);
			
		
		
		
		
		
		
		
		int numberOfNewFails = agent.getTestDataCollector().getNumberOfFailVerdictsSeen() -  numberOfFailingCheck ;
		
		int numberOfNewPasses = agent.getTestDataCollector().getNumberOfPassVerdictsSeen() - numberOfPassingCheck;
		
		
		numberOfPassingCheck += numberOfNewPasses;
		numberOfFailingCheck += numberOfNewFails ;
		
		
		if (g2.getStatus().success()) {
			testCompletedSuccessfully = 1;
		}
		else if (g2.getStatus().failed()) {
			testCompletedSuccessfully = 0;
		}
		else {
			testCompletedSuccessfully = 2;
		}
		
		
		System.out.println("test Completed Successfully:	>>> "+ testCompletedSuccessfully);		// 0 for fail, 1 for success, 2 otherwise (in progress) 

		
		
		
		
		ScalarTracingEvent scalarValues1 = new ScalarTracingEvent(
				
				new Pair("newTests", (collector.getNumberOfPassVerdictsSeen() + collector.getNumberOfFailVerdictsSeen())),
				new Pair("newPasses", numberOfNewPasses),
				new Pair("newFails", numberOfNewFails),
				new Pair("seed", seed),
				new Pair("testCompletedSuccessfully", testCompletedSuccessfully)
				
				
				) ;		

		agent.getTestDataCollector().registerEvent(agent.getId(), scalarValues1);
		
	
		System.out.println("** Rest items: Number of passes: "  + collector.getNumberOfPassVerdictsSeen()) ;
		System.out.println("** Rest items: Number of violations: "  + collector.getNumberOfFailVerdictsSeen()) ;
		collector.getTestAgentTrace(agent.getId()) ;

		//assertTrue(collector.getNumberOfFailVerdictsSeen() == 0) ;
		collector.saveTestAgentScalarsTraceAsCSV(agent.getId(), "seed."+seed+".interact.Testing rest items.csv");
		
		
		
		
		long elapsedTimeMillis = System.currentTimeMillis()-start;
		float elapsedTimeSec = elapsedTimeMillis/1000F;
		
		System.out.println(">>> Execution Time: " + elapsedTimeSec + "sec");
		
	}
	
	
	
	
	
}
