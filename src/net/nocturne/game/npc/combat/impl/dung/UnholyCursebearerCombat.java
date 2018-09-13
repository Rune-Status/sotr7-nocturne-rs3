package net.nocturne.game.npc.combat.impl.dung;

import net.nocturne.game.*;
import net.nocturne.game.Hit.HitLook;
import net.nocturne.game.TemporaryAtributtes.Key;
import net.nocturne.game.npc.NPC;
import net.nocturne.game.npc.combat.CombatScript;
import net.nocturne.game.npc.combat.NPCCombatDefinitions;
import net.nocturne.game.npc.dungeonnering.DungeonBoss;
import net.nocturne.game.player.Player;
import net.nocturne.game.player.Skills;
import net.nocturne.game.tasks.WorldTask;
import net.nocturne.game.tasks.WorldTasksManager;
import net.nocturne.utils.Utils;

public class UnholyCursebearerCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
		return new Object[] { "Unholy cursebearer" };
	}

	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = Utils.isOnRange(target.getX(), target.getY(),
				target.getSize(), npc.getX(), npc.getY(), npc.getSize(), 0) ? Utils
				.random(2) : 0;
		if (target instanceof Player
				&& target.getTemporaryAttributtes().get(
						Key.UNHOLY_CURSEBEARER_ROT) == null) {
			target.getTemporaryAttributtes().put(Key.UNHOLY_CURSEBEARER_ROT, 1);
			final Player player = (Player) target;
			player.getPackets().sendGameMessage(
					"An undead rot starts to work at your body.");
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					Integer value = (Integer) target.getTemporaryAttributtes()
							.get(Key.UNHOLY_CURSEBEARER_ROT);
					if (player.hasFinished()
							|| npc.hasFinished()
							|| !((DungeonBoss) npc).getManager().isAtBossRoom(
									player) || value == null) {
						target.getTemporaryAttributtes().remove(
								Key.UNHOLY_CURSEBEARER_ROT);
						stop();
						return;
					}
					int damage = 20 * value;
					for (int stat = 0; stat < 7; stat++) {
						if (stat == Skills.HITPOINTS)
							continue;
						int drain = Utils.random(5) + 1;
						if (stat == Skills.PRAYER)
							player.getPrayer().drain(drain * 10);
						player.getSkills().drainLevel(stat, drain);
					}
					int maxDamage = player.getMaxHitpoints() / 10;
					if (damage > maxDamage)
						damage = maxDamage;
					if (value == 6)
						player.getPackets()
								.sendGameMessage(
										"The undead rot can now be cleansed by the unholy font.");
					player.applyHit(new Hit(npc, damage, HitLook.REGULAR_DAMAGE));
					player.setNextGraphics(new Graphics(2440));
					target.getTemporaryAttributtes().put(
							Key.UNHOLY_CURSEBEARER_ROT, value + 1);
				}

			}, 0, 12);
		}
		switch (attackStyle) {
		case 0:
			boolean multiTarget = Utils.random(2) == 0;
			npc.setNextAnimation(new Animation(multiTarget ? 13176 : 13175));
			if (multiTarget) {
				npc.setNextGraphics(new Graphics(2441));
				for (Entity t : npc.getPossibleTargets()) {
					World.sendProjectile(npc, t, 88, 50, 30, 41, 40, 0, 0);
					delayHit(
							npc,
							1,
							t,
							getMagicHit(
									npc,
									getMaxHit(
											npc,
											(int) (npc
													.getMaxHit(NPCCombatDefinitions.MAGE) * 0.6),
											NPCCombatDefinitions.MAGE, t)));
				}
			} else {
				World.sendProjectile(npc, target, 88, 50, 30, 41, 30, 0, 0);
				delayHit(
						npc,
						1,
						target,
						getMagicHit(
								npc,
								getMaxHit(npc, NPCCombatDefinitions.MAGE,
										target)));
			}
			break;
		case 1:
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(
					npc,
					0,
					target,
					getMeleeHit(npc,
							getMaxHit(npc, NPCCombatDefinitions.MELEE, target)));
			break;
		}
		return npc.getAttackSpeed();
	}
}