/*
 * Override lunr.js for French language for Ametys
 */

(function(){

/*
 * Pipeline for elision
 */
lunr.elision = function (token) {
  var elisionRegex = /^(qu|Qu|QU|[ldmtnsjcLDMTNSJC])'/;
  var tokens = token.split(elisionRegex);
  var lastIndex = tokens.length - 1;
  
  return tokens[lastIndex];
}

lunr.Pipeline.registerFunction(lunr.elision, 'elision');

})();