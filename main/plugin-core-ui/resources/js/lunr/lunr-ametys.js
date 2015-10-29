/*
 * Override lunr.js for specifications for Ametys
 */

(function(){
	
/*
 * Change the tokenizer seperator (include HTML tags)
 */
lunr.tokenizer.seperator = /(?:[\s\-\.,;:\(\)]+)|(?:<\w+\/>)+|(?:<\w+>)+|(?:<\/\w+>)/g;

/*
 * Pipeline for deephasize the tokens
 */
lunr.deemphasize = function (token) {
  return Ext.String.deemphasize(token);
}

lunr.Pipeline.registerFunction(lunr.deemphasize, 'deemphasize');

/*
 * Change the original function in order to make a OR based query with multiple
 * words, e.g. `idx.search('foo bar')` will run a search for documents containing 
 * either 'foo' or 'bar'.
 */
lunr.Index.prototype.search = function (query) {
  var queryTokens = this.pipeline.run(lunr.tokenizer(query)),
      queryVector = new lunr.Vector,
      documentSets = [],
      fieldBoosts = this._fields.reduce(function (memo, f) { return memo + f.boost }, 0)

  var hasSomeToken = queryTokens.some(function (token) {
    return this.tokenStore.has(token)
  }, this)

  if (!hasSomeToken) return []

  queryTokens
    .forEach(function (token, i, tokens) {
      var tf = 1 / tokens.length * this._fields.length * fieldBoosts,
          self = this

      var set = this.tokenStore.expand(token).reduce(function (memo, key) {
        var pos = self.corpusTokens.indexOf(key),
            idf = self.idf(key),
            similarityBoost = 1,
            set = new lunr.SortedSet

        // if the expanded key is not an exact match to the token then
        // penalise the score for this key by how different the key is
        // to the token.
        if (key !== token) {
          var diff = Math.max(3, key.length - token.length)
          similarityBoost = 1 / Math.log(diff)
        }

        // calculate the query tf-idf score for this token
        // applying an similarityBoost to ensure exact matches
        // these rank higher than expanded terms
        if (pos > -1) queryVector.insert(pos, tf * idf * similarityBoost)

        // add all the documents that have this key into a set
        Object.keys(self.tokenStore.get(key)).forEach(function (ref) { set.add(ref) })

        return memo.union(set)
      }, new lunr.SortedSet)

      documentSets.push(set)
    }, this)

  var documentSet = documentSets.reduce(function (memo, set) {
    // here is the modification: union instead of
  	// intersection to have more results (OR query)
    return memo.union(set)
  })

  return documentSet
    .map(function (ref) {
      return { ref: ref, score: queryVector.similarity(this.documentVector(ref)) }
    }, this)
    .sort(function (a, b) {
      return b.score - a.score
    })
}

})();