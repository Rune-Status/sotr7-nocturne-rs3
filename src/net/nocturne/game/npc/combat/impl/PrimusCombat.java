package net.nocturne.game.npc.combat.impl;

import java.util.ArrayList;
import java.util.Random;

import net.nocturne.game.Animation;
import net.nocturne.game.Entity;
import net.nocturne.game.Graphics;
import net.nocturne.game.Hit;
import net.nocturne.game.Hit.HitLook;
import net.nocturne.game.World;
import net.nocturne.game.WorldTile;
import net.nocturne.game.npc.NPC;
import net.nocturne.game.npc.combat.CombatScript;
import net.nocturne.game.player.Player;
import net.nocturne.game.tasks.WorldTask;
import net.nocturne.game.tasks.WorldTasksManager;

/**
 * @author Nosz
 * 
 * 
 * 
 */

public class PrimusCombat extends CombatScript {
	public int damageBuff;
	int Stage;

	@Override
	public Object[] getKeys() {
		return new Object[] { 17149 };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final ArrayList<Entity> possibleTargets = npc.getPossibleTargets();
		if (npc.getPossibleTargets().size() != 0) {
			for (final Entity t : possibleTargets) {
				if (t instanceof Player) {
					final Player p = (Player) t;
					if (npc.getHitpoints() > (npc.getMaxHitpoints() / 4) * 3) { // higher
																				// then
																				// 75%
						p.getTemporaryAttributtes().put("PrimusStage", 0);
					}
					if (npc.getHitpoints() <= (npc.getMaxHitpoints() / 4) * 3
							&& (int) p.getTemporaryAttributtes().get(
									"PrimusStage") == 0) { // 75%
						p.getTemporaryAttributtes().put(
								"PrimusStage",
								(int) p.getTemporaryAttributtes().get(
										"PrimusStage") + 1);
						damageBuff = 2;
					}
					if (npc.getHitpoints() <= (npc.getMaxHitpoints() / 4) * 2
							&& (int) p.getTemporaryAttributtes().get(
									"PrimusStage") == 1) { // 50%
						p.getTemporaryAttributtes().put(
								"PrimusStage",
								(int) p.getTemporaryAttributtes().get(
										"PrimusStage") + 1);
						damageBuff = 4;
					}
					if (npc.getHitpoints() <= (npc.getMaxHitpoints() / 4)
							&& (int) p.getTemporaryAttributtes().get(
									"PrimusStage") == 2) { // 25%
						damageBuff = 5;
					}
					WorldTasksManager.schedule(new WorldTask() {// NORMAL ATTACK
								Random rn = new Random();
								int max = 1000;
								int min = 750;
								int range = max - min + 1;
								int randomNum = rn.nextInt(range) + min;
								private int gameTick;

								@Override
								public void run() {
									gameTick++;
									if (gameTick == 1) {
										npc.setNextAnimation(new Animation(
												20277));
										npc.setNextGraphics(new Graphics(3977));
									}
									if (gameTick == 2) {
										World.sendProjectile(npc, t, 3978, 10,
												6, 40, 5, 0, 0);
										p.applyHit(new Hit(p, randomNum,
												HitLook.MAGIC_DAMAGE, 0));

									}
								}

							}, 0, 0);
					WorldTasksManager.schedule(new WorldTask() { // AOE SPELL
								int x;
								int y;;
								int max = 3000;
								int min = 1000;
								Random rn = new Random();
								int range = max - min + 1;
								int randomNum = rn.nextInt(range) + min;
								private int gameTick;

								@Override
								public void run() {
									gameTick++;
									if (gameTick == 1) {
										npc.setNextAnimation(new Animation(
												20277));
										x = p.getX();
										y = p.getY();

									}
									if (gameTick == 4) {
										World.sendGraphics(null, new Graphics(
												3974), new WorldTile(x, y, 1));
									}
									if (gameTick == 5) {
										if (target.getX() == x
												&& target.getY() == y) {
											p.applyHit(new Hit(p, randomNum
													* damageBuff,
													HitLook.MAGIC_DAMAGE, 0));
										}

									}
								}

							}, 0, 0);
				}
			}
		}

		return npc.getAttackSpeed();
	}
}
