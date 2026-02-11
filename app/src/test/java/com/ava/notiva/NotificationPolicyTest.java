package com.ava.notiva;

import static org.junit.Assert.*;

import com.ava.notiva.model.CollapseStrategy;
import com.ava.notiva.model.NotificationPolicy;
import com.ava.notiva.model.SoundStrategy;

import org.junit.Test;

/**
 * Unit tests for {@link NotificationPolicy}, {@link CollapseStrategy}, and {@link SoundStrategy}.
 */
public class NotificationPolicyTest {

    // ==================== NotificationPolicy Constructor ====================

    @Test
    public void constructor_setsAllFields() {
        NotificationPolicy policy = new NotificationPolicy(
                CollapseStrategy.REPLACE, SoundStrategy.EVERY_FIRE, 10);

        assertEquals(CollapseStrategy.REPLACE, policy.getCollapseStrategy());
        assertEquals(SoundStrategy.EVERY_FIRE, policy.getSoundStrategy());
        assertEquals(10, policy.getMaxActiveNotifications());
    }

    @Test
    public void constructor_withNullStrategy_allowsNull() {
        NotificationPolicy policy = new NotificationPolicy(null, null, 0);

        assertNull(policy.getCollapseStrategy());
        assertNull(policy.getSoundStrategy());
        assertEquals(0, policy.getMaxActiveNotifications());
    }

    // ==================== NotificationPolicy.defaults() ====================

    @Test
    public void defaults_returnsStackCollapse() {
        NotificationPolicy policy = NotificationPolicy.defaults();
        assertEquals(CollapseStrategy.STACK, policy.getCollapseStrategy());
    }

    @Test
    public void defaults_returnsOnceSound() {
        NotificationPolicy policy = NotificationPolicy.defaults();
        assertEquals(SoundStrategy.ONCE, policy.getSoundStrategy());
    }

    @Test
    public void defaults_returnsFiveMaxActive() {
        NotificationPolicy policy = NotificationPolicy.defaults();
        assertEquals(5, policy.getMaxActiveNotifications());
    }

    @Test
    public void defaults_returnsNewInstanceEachCall() {
        NotificationPolicy policy1 = NotificationPolicy.defaults();
        NotificationPolicy policy2 = NotificationPolicy.defaults();
        assertNotSame("defaults() should return new instance each time", policy1, policy2);
    }

    // ==================== NotificationPolicy.toString() ====================

    @Test
    public void toString_containsAllFields() {
        NotificationPolicy policy = new NotificationPolicy(
                CollapseStrategy.SUMMARY_ONLY, SoundStrategy.SUMMARY_ONLY, 3);

        String result = policy.toString();

        assertTrue(result.contains("SUMMARY_ONLY"));
        assertTrue(result.contains("3"));
        assertTrue(result.contains("NotificationPolicy"));
    }

    @Test
    public void toString_defaults_containsDefaultValues() {
        String result = NotificationPolicy.defaults().toString();

        assertTrue(result.contains("STACK"));
        assertTrue(result.contains("ONCE"));
        assertTrue(result.contains("5"));
    }

    // ==================== CollapseStrategy Enum ====================

    @Test
    public void collapseStrategy_hasThreeValues() {
        assertEquals(3, CollapseStrategy.values().length);
    }

    @Test
    public void collapseStrategy_valuesExist() {
        assertNotNull(CollapseStrategy.STACK);
        assertNotNull(CollapseStrategy.REPLACE);
        assertNotNull(CollapseStrategy.SUMMARY_ONLY);
    }

    @Test
    public void collapseStrategy_valueOf_validNames() {
        assertEquals(CollapseStrategy.STACK, CollapseStrategy.valueOf("STACK"));
        assertEquals(CollapseStrategy.REPLACE, CollapseStrategy.valueOf("REPLACE"));
        assertEquals(CollapseStrategy.SUMMARY_ONLY, CollapseStrategy.valueOf("SUMMARY_ONLY"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void collapseStrategy_valueOf_invalidName_throwsException() {
        CollapseStrategy.valueOf("INVALID");
    }

    @Test
    public void collapseStrategy_ordinals_maintainOrder() {
        assertTrue(CollapseStrategy.STACK.ordinal() < CollapseStrategy.REPLACE.ordinal());
        assertTrue(CollapseStrategy.REPLACE.ordinal() < CollapseStrategy.SUMMARY_ONLY.ordinal());
    }

    // ==================== SoundStrategy Enum ====================

    @Test
    public void soundStrategy_hasThreeValues() {
        assertEquals(3, SoundStrategy.values().length);
    }

    @Test
    public void soundStrategy_valuesExist() {
        assertNotNull(SoundStrategy.ONCE);
        assertNotNull(SoundStrategy.EVERY_FIRE);
        assertNotNull(SoundStrategy.SUMMARY_ONLY);
    }

    @Test
    public void soundStrategy_valueOf_validNames() {
        assertEquals(SoundStrategy.ONCE, SoundStrategy.valueOf("ONCE"));
        assertEquals(SoundStrategy.EVERY_FIRE, SoundStrategy.valueOf("EVERY_FIRE"));
        assertEquals(SoundStrategy.SUMMARY_ONLY, SoundStrategy.valueOf("SUMMARY_ONLY"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void soundStrategy_valueOf_invalidName_throwsException() {
        SoundStrategy.valueOf("INVALID");
    }

    @Test
    public void soundStrategy_ordinals_maintainOrder() {
        assertTrue(SoundStrategy.ONCE.ordinal() < SoundStrategy.EVERY_FIRE.ordinal());
        assertTrue(SoundStrategy.EVERY_FIRE.ordinal() < SoundStrategy.SUMMARY_ONLY.ordinal());
    }

    // ==================== Integration: All Combinations ====================

    @Test
    public void allCombinations_canBeConstructed() {
        for (CollapseStrategy cs : CollapseStrategy.values()) {
            for (SoundStrategy ss : SoundStrategy.values()) {
                NotificationPolicy policy = new NotificationPolicy(cs, ss, 1);
                assertEquals(cs, policy.getCollapseStrategy());
                assertEquals(ss, policy.getSoundStrategy());
                assertEquals(1, policy.getMaxActiveNotifications());
            }
        }
    }
}
