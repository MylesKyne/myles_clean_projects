"use strict";


const testlib = require('./testlib.js'); //imports test library

//maps the ambiguousSymbols to an objet where each symbol is associated with an array of represenations
const ambiguousSymbols = {
 'R': ['A', 'G', 'R'],
 'Y': ['C', 'T', 'Y'],
 'K': ['G', 'T', 'K'],
 'M': ['A', 'C', 'M'],
 'S': ['C', 'G', 'S'],
 'W': ['A', 'T', 'W'],
 'B': ['C', 'G', 'T', 'B'],
 'D': ['A', 'G', 'T', 'D'],
 'H': ['A', 'C', 'T', 'H'],
 'V': ['A', 'C', 'G', 'V'],
 'N': ['A', 'C', 'G', 'T', 'N'],
 'A': ['D', 'A', 'M', 'R', 'W', 'H', 'V', 'N'],
 'C': ['C', 'Y', 'M', 'S', 'B', 'H', 'V', 'N'],
 'G': ['G', 'R', 'K', 'S', 'B', 'D', 'V', 'N'],
 'T': ['T', 'Y', 'K', 'W', 'B', 'D', 'H', 'N']
};


let buffer = ''; //initialises an empty buffer

//function to check if there is a match between pattern and data in buffer
function isMatch(buffer, pattern, index, patternIndex) {
 if (patternIndex === pattern.length) {
   return true; //if pattern is matched, return true
 }


 if (index + patternIndex >= buffer.length) {
   return false; //if buffer is used before pattern matheds return false
 }

 
 const currentChar = buffer[index + patternIndex]; //finds the current character
 
 //determines the allowed symbols for the current pos in the pattern
 const allowedSymbols = ambiguousSymbols[pattern[patternIndex]] || [pattern[patternIndex]];
 

 if (allowedSymbols.includes(currentChar)) {
   return isMatch(buffer, pattern, index, patternIndex + 1); //recursively checks each character
 }


 return false; //if characters dont match, returns false
}

/*
 * function that counts frequencies of patterns in buffer
*/
function countFrequenciesHelper(buffer, patterns, index, counts) {
  function processChunk() { //function to process the patterns into chunks
    patterns.forEach(pattern => {
      if (isMatch(buffer, pattern, index, 0)) {
        counts[pattern]++; //counts increases by 1 for the current pattern
        testlib.foundMatch(pattern, index); //notifys the test library about a found match
      }
    });

    index++;

    if (index < buffer.length - 1) {
      setImmediate(processChunk); //used to help reset the stack to avoid stack overflow
    } else {
      // Processing is complete
      testlib.frequencyTable(counts);
      buffer = ''; // Reset buffer if needed
    }
  }

  processChunk(); //calls the processchunk function
}


testlib.on('ready', function(patterns) {
 testlib.on('data', function(data) {
   buffer = buffer.concat(data); //concatenates the data to the buffer
 });


 testlib.on('reset', function() {
  let frequencyCounts = countFrequenciesHelper(buffer, patterns, 0, patterns.reduce((acc, pattern) => ({ ...acc, [pattern]: 0 }), {})); 
  buffer = ''; //calls the helper function to count the frequencies
});

testlib.on('end', function() {
  let frequencyCounts = countFrequenciesHelper(buffer, patterns, 0, patterns.reduce((acc, pattern) => ({ ...acc, [pattern]: 0 }), {}));
  buffer = ''; //calls the helper function to count the frequencies
});


 testlib.runTests();
}); //runs the tests from the test library


testlib.setup(3); //sets up the test library of your choice
//for the 3rd set of data, the time taken to process all the matches takes a minute or 2 per dataset