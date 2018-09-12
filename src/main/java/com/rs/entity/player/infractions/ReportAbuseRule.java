package com.rs.entity.player.infractions;
/*
 * This file is part of RuneSource.
 *
 * RuneSource is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RuneSource is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RuneSource.  If not, see <http://www.gnu.org/licenses/>.
 */

public enum ReportAbuseRule {
    OffensiveLanguage(0),
    ItemScamming(1),
    PasswordScamming(2),
    BugAbuse(3),
    JagexStaffImpersonation(4),
    AccountSharingOrTrading(5),
    Macroing(6),
    MultipleLoggingIn(7),
    EncouragingRuleBreaking(8),
    MisuseOfCustomerSupport(9),
    Advertising(10),
    RealWorldItemTrading(11);

    private final int ruleId;

    ReportAbuseRule(int ruleId) {
        this.ruleId = ruleId;
    }

    public static ReportAbuseRule ofId(int ruleId) {
        for (ReportAbuseRule rule : values()) {
            if (rule.getRuleId() == ruleId) {
                return rule;
            }
        }
        throw new IllegalArgumentException("invalid rule id");
    }

    public int getRuleId() {
        return ruleId;
    }
}
