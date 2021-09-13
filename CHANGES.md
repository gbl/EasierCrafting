Version 1.7
===========

Fix a bug where shapeless recipes, like concrete or netherite ingots, were shown
in the 2x2 crafting screen even though they need more than 4 ingredients.

Hide all recipes for a split second before updating (time is configurable between
0 and 2000 ms) so people don't accidentially click the new recipe right after
an update. Because of this, reduce the default update time from 5 to 2 seconds.
