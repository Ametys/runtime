/*
 * Override lunr.js for English language for Ametys
 */

(function(){

/*
 * Pipeline for elision
 */
lunr.elision = function (token) {
  var elisionRegex = /'[sS]$/;
  var tokens = token.split(elisionRegex);
  
  return tokens[0];
}

lunr.Pipeline.registerFunction(lunr.elision, 'elision');

})();