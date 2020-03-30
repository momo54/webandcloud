/**
 * 
 */

var DiceGame={
		turn : 0,
		d1 : 0,
		d2 : 0,
		name : 'spock',
		score : 0,

		play = function() {
			if (game.turn < 10) {
				game.d1 = Math.floor((Math.random() * 5) + 1);
				game.d2 = Math.floor((Math.random() * 5) + 1);
				if (game.d1 + game.d2 == 7) {
					game.score += 10;
				}
				game.turn++;
			}
		}
}
