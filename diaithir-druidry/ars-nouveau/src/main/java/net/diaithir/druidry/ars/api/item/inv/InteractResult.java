package net.diaithir.druidry.ars.api.item.inv;

import net.diaithir.druidry.ars.common.items.ItemScroll;

public record InteractResult(ItemScroll.SortPref sortPref, boolean valid) {
}
