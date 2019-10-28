!function(){
    var st = {version: "0.0.3"};

/**
 * util stub.
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 */
st.util = {};

/**
 * Simple hash-based object cache.
 *
 * Adapted from:
 * http://markdaggett.com/blog/2012/03/28/
 * client-side-request-caching-with-javascript/
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @returns {object} object literal with a add, get, getKey, and exists property
 * 
 * @example
 * var cache = st.util.cache();
 * var cacheKey = cache.getKey(myObject);
 * if (cache.exists(cacheKey)) {
 *  var cachedObject = cache.get(cacheKey);
 * } else {
 *  var cachedObject = myObject;
 *  cache.add(cacheKey, cachedObject);
 * }
 */
st.util.cache = function () {
    var cache = {},
    keys = [],

    /**
     * Returns an element's index in an array or -1.
     * 
     * @param {object[]} arr An element array
     * @param {object} obj An element
     * @returns {number} the element's index or -1
     */
    indexOf = function (arr, obj) {
        var len = arr.length;
        for (var i = 0; i < len; i++) {
            if (arr[i] === obj) {
                return i;
            }
        }
        return -1;
    },

    /**
     * Returns a string representation of any input.
     * 
     * @param {object} opts An input to stringify
     * @returns {string} the stringified input object
     */
    serialize = function (opts) {
        if ((opts).toString() === "[object Object]") {
            return $.param(opts);
        } else {
            return (opts).toString();
        }
    },

     /**
     * Removes an element from the cache via its key.
     * 
     * @param {string} key The element's key
     */
    remove = function (key) {
        var t;
        if ((t = indexOf(keys, key)) > -1) {
            keys.splice(t, 1);
            delete cache[key];
        }
    },

    /**
     * Removes all elements from the cache.
     */
    removeAll = function () {
        cache = {};
        keys = [];
    },

    /**
     * Adds an element to the cache.
     * 
     * @param {string} key The element's key
     * @param {object} obj The element to be added
     */
    add = function (key, obj) {
        if (keys.indexOf(key) === -1) {
            keys.push(key);
        }
        cache[key] = obj;
    },

    /**
     * Checks whether a key has already been added to the cache.
     * 
     * @param {string} key The element's key
     * @returns {boolean} whether the key exists in the cache
     */
    exists = function (key) {
        return cache.hasOwnProperty(key);
    },

    /**
     * Removes a selected or all elements from the cache.
     * 
     * @returns {object[]} the purged cache array
     */
    purge = function () {
        if (arguments.length > 0) {
            remove(arguments[0]);
        } else {
            removeAll();
        }
        return $.extend(true, {}, cache);
    },

    /**
     * Returns matching keys from the cache in an array.
     * 
     * @param {string} str The query key (string)
     * @returns {string[]} the array of matching keys
     */
    searchKeys = function (str) {
        var keys = [];
        var rStr;
        rStr = new RegExp('\\b' + str + '\\b', 'i');
        $.each(keys, function (i, e) {
            if (e.match(rStr)) {
                keys.push(e);
            }
        });
        return keys;
    },

    /**
     * Returns the element for a given key.
     * 
     * @param {string} key The element's key
     * @returns {object} the key's cached object
     */
    get = function (key) {
        var val;
        if (cache[key] !== undefined) {
            if ((cache[key]).toString() === "[object Object]") {
                val = $.extend(true, {}, cache[key]);
            } else {
                val = cache[key];
            }
        }
        return val;
    },

    /**
     * Returns the string representation of the element.
     * 
     * @param {object} opts The element to be stringified
     * @returns {string} the string representation fo the element
     */
    getKey = function (opts) {
        return serialize(opts);
    },

    /**
     * Returns all keys stored in the cache.
     * 
     * @returns {string[]} the array of keys
     */
    getKeys = function () {
        return keys;
    };

    // reference visible (public) functions as properties
    return {
        add: add,
        get: get,
        getKey: getKey,
        exists: exists,
    };
};

/**
 * Color object that consistently returns one of six different colors 
 * for a given identifier.
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @returns {object} object literal with a get and remove property
 */
st.util.colors = function () {
    var colors = {
        0: "red",
        1: "blue",
        2: "green",
        3: "orange",
        4: "yellow",
        5: "black"
    },

    mapping = {};    // stores the id - color mappings
    mapping.size = function() {
        var size = -1, key;
        for (key in this) {
            if (this.hasOwnProperty(key)) {
                size++;
            }
        }
        return size;
    };

    /**
     * Gets the color for the identifier or - if id is unassigned - returns
     * a new color from the color hash.
     * 
     * @param {int} id A series identifier
     * @returns {string} the color string for the identifier
     */
    var get = function (id) {
        if (mapping[id]) {
            return mapping[id];
        }
        var col = next();
        mapping[id] = col;
        return mapping[id];
    },
    
    /**
     * Removes the color for the identifier from the mapping.
     * 
     * @param {int} id An series identifier
     */
    remove = function (id) {
        if (mapping[id]) {
            delete mapping[id];
        }
    },

    /**
     * Returns the color string based on the running index. Resets the
     * index if it exceeds the color hash.
     * 
     * @returns {string} the color string
     */
    next = function () {
        var ncolors = Object.keys(colors).length;
        var nmappings = mapping.size();
        var index = nmappings % ncolors;
        return colors[index];
    };

    // reference visible (public) functions as properties
    return {
        get: get,
        remove: remove
    };
};

/**
 * Simple hash code generator for strings.
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @param {string} str A string to be hashed
 * @returns {number} the hashed string
 */
st.util.hashcode = function (str) {
    var hash = 0, i, chr, len;
    if (str.length == 0) return hash;
    for (i = 0, len = str.length; i < len; i++) {
        chr = str.charCodeAt(i);
        hash = ((hash << 5) - hash) + chr;
        hash |= 0; // convert to 32bit integer
    }
    return hash;
};

/**
 * Helper function to resolve the order of domain extrema based on the 
 * direction of the scale, e.g. for inverted axes the min and max values 
 * need to be inverted.
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @param {object} scale A d3 scale
 * @param {number[]} array An array of length two with a min/max pair
 * @returns {number[]} the sorted array
 */
st.util.domain = function (scale, array) {
    var domain = scale.domain();
    if (domain[0] > domain[1]) {
        return [
            array[1],
            array[0]
        ];
    }
    return [
        array[0],
        array[1]
    ];
};

/**
 * SVG molecule renderer for MDL Molfiles. The header block and 
 * connection table are loosely parsed according to Elsevier MDL's V2000
 * format.
 * 
 * The molecule title is taken from the header block.
 * 
 * The two dimensional coordinates, symbol, charge, and mass difference
 * information is extracted from the atom block. 
 * 
 * Connectivity and stereo information is extracted from the bond block.
 * Single, double, and triple bonds as well as symbols for wedge, hash,
 * and wiggly bonds are supported.
 * 
 * The renderer uses the CPK coloring convention.
 *
 * Initializes the renderer setting the width and height of 
 * the viewport. The width and height should include a margin 
 * of 10 px, which is applied all around by default.
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @param {number} width A width of the viewport
 * @param {number} height A height of the viewport
 * @returns {object} object literal with a draw property
 */
st.util.mol2svg = function (width, height) {

    var w = width || 200,   // width of the panel
        h = height || 200,  // height of the panel
        x = null,           // linear d3 x scale function
        y = null,           // linear d3 y scale function
        avgL = 0,   // scaled average bond length (for font size scaling)
        cache = st.util.cache();

    /**
     * Loads the molfile data asynchronously, parses the file and 
     * creates the SVG. The SVG is appended to the element of the 
     * given identifier.    
     * 
     * @param {string} molfile A URL of the MDL molfile (REST web service)
     * @param {string} id An identifier of the element 
     * @returns {object} a XHR promise
     */
    var draw = function (molfile, id) {
        var jqxhr;
        var el = d3.select(id);
        var cacheKey = cache.getKey(molfile);
        if (cache.exists(cacheKey)) {
            var text = cache.get(cacheKey);
            parse(text, el);
        } else {
            jqxhr = $.when(
                $.get(molfile)
            )
            .fail(function() {
                console.log('Request failed for: ' + molfile);
            })
            .then(function(text) {
                cache.add(cacheKey, text);
                try {
                    parse(text, el);
                } catch (err) {
                    console.log('Mol2Svg Error:' + err);
                    el.html('');
                }
            });
        }
        return jqxhr;
    };

    /**
     * Parses the molfile, extracting the molecule title from the 
     * header block, two dimensional coordinates, symbol, charge, 
     * and mass difference information extracted from the atom block,
     * connectivity and stereo information from the bond block.
     *
     * @param {string} molfile A URL to the MDL molfile (REST web service)
     * @param {string} id An element identifier
     */
    var parse = function (molfile, el) {
        var lines = molfile.split(/\r\n|\n/),
            // title = lines[1],
            counter = lines[3].match(/\d+/g),
            nAtoms = parseFloat(counter[0]),
            nBonds = parseFloat(counter[1]);

        var atoms = atomBlock(lines, nAtoms),           // get all atoms
            bonds = bondBlock(lines, nAtoms, nBonds);   // get all bonds
        propsBlock(lines, atoms, nAtoms + nBonds);      // get properties

        var graph = initSvg(atoms, el);                 // layout SVG
        drawBonds(atoms, bonds, graph);
        drawAtoms(atoms, avgL, graph);
    };

    /**
     * Parses the atom block line by line.
     *
     * @param {string[]} lines A molfile line array
     * @param {number} nAtoms The total number of atoms
     * @returns {object[]} associative array of atom objects
     */
    var atomBlock = function (lines, nAtoms) {
        var atoms = [];
        var offset = 4; // the first three lines belong to the header block
        for (var i = offset; i < nAtoms + offset; i++) {
            var atom = lines[i].match(/-*\d+\.\d+|\w+/g);
            atoms.push({
                x: parseFloat(atom[0]),
                y: parseFloat(atom[1]),
                symbol: atom[3],
                mass: 0,    // deprecated
                charge: 0   // deprecated
            });
        }
        return atoms;
    };

    /**
     * Parses the bond block line by line.
     * 
     * @param {string[]} lines A molfile line array
     * @param {number} nAtoms The total number of atoms
     * @param {number} nBonds The total number of bonds
     * @returns {object[]} associative array of bond objects
     */
    var bondBlock = function (lines, nAtoms, nBonds) {
        var bonds = [];
        var offset = 4; // the first three lines belong to the header block
        for (var j = nAtoms + offset; j < nAtoms + nBonds + offset; j++) {
            var bond = lines[j].match(/\d+/g);
            bonds.push({
                // adjust to '0', atom counter starts at '1'
                a1: parseInt(bond[0]) - 1,  
                a2: parseInt(bond[1]) - 1,
                // values 1, 2, 3
                order: parseInt(bond[2]),
                // values 0 (plain),1 (wedge),4 (wiggly),6 (hash)                
                stereo: parseInt(bond[3])
            });
        }
        return bonds;
    };

    /**
     * Parses the properties block line by line.
     * 
     * @param {string[]} lines A molfile line array
     * @param {object[]} atoms An array of atom objects
     * @param {number} nAtomsBonds The total number of atoms and bonds
     */
    var propsBlock = function (lines, atoms, nAtomsBonds) {
        var offset = 4; // the first three lines belong to the header block
        for (var k = nAtomsBonds + offset; k < lines.length; k++) {
            if (lines[k].indexOf('M  ISO') !== -1) {
                var props = lines[k].match(/-*\d+/g);
                for (var l = 0, m = 1; l < props[0]; l++, m += 2) {
                    atoms[props[m] - 1].mass = parseInt(props[m + 1], 10);
                }
            } else if (lines[k].indexOf('M  CHG') !== -1) {
                var props = lines[k].match(/-*\d+/g);
                for (var l = 0, m = 1; l < props[0]; l++, m += 2) {
                    atoms[props[m] - 1].charge = parseInt(props[m + 1], 10);
                }
            }
        }
    };

    /**
     * Initializes the viewport and appends it to the element identified
     * by the given identifier. The linear d3 x- and y-scales are set 
     * to translate from the viewport coordinates to the mol coordinates.
     * 
     * @param {object[]} atoms An array of atom objects
     * @param {string} id An element identifier
     * @returns {object} the initialized SVG element
     */
    var initSvg = function (atoms, el) {
        // x minimum and maximum
        var xExtrema = d3.extent(atoms, function (atom) {
            return atom.x;
        });
        // y minimum and maximum
        var yExtrema = d3.extent(atoms, function (atom) { 
            return atom.y;
        });

        // dimensions of molecule graph
        var m = [20, 20, 20, 20];   // margins
        var wp = w - m[1] - m[3];   // width
        var hp = h - m[0] - m[2];   // height

        // maintain aspect ratio: divide/multiply height/width by the ratio (r)
        var r = (xExtrema[1] - xExtrema[0]) / (yExtrema[1] - yExtrema[0]);
        if (r > 1) {
            hp /= r;
        } else {
            wp *= r;
        }

        // X scale will fit all values within pixels 0-w
        x = d3.scale.linear().domain([xExtrema[0], xExtrema[1]]).range([0, wp]);
        // Y scale will fit all values within pixels h-0
        y = d3.scale.linear().domain([yExtrema[0], yExtrema[1]]).range([hp, 0]);

        // add an SVG element with the desired dimensions
        // and margin and center the drawing area
        var graph = el.append('svg:svg')
            .attr('width', wp + m[1] + m[3])
            .attr('height', hp + m[0] + m[2])
            .append('svg:g')
            .attr('transform', 'translate(' + m[3] + ',' + m[0] + ')');

        return graph;
    };

    /**
     * Draws the bonds onto the SVG element. Note that the bonds are drawn
     * first before anything else is added.
     * 
     * @param {object[]} atoms An array of atom objects
     * @param {object[]} bonds An array of bond objects
     * @param {object} graph A SVG element
     */
    var drawBonds = function (atoms, bonds, graph) {
        for (var i = 0; i < bonds.length; i++) {
            var a1 = atoms[bonds[i].a1],
                a2 = atoms[bonds[i].a2];

            // apply backing by calculating the unit vector and
            // subsequent scaling: shortens the drawn bond
            var dox = a2.x - a1.x,
                doy = a2.y - a1.y,
                l = Math.sqrt(dox * dox + doy * doy),
                dx = (dox / l) * (0.2),
                dy = (doy / l) * (0.2);

            // get adjusted x and y coordinates
            var x1 = a1.x + dx,
                y1 = a1.y + dy,
                x2 = a2.x - dx,
                y2 = a2.y - dy;

            // update average bond length for font scaling
            avgL += distance(x(x1), y(y1), x(x2), y(y2));

            var off,    // offset factor for stereo bonds
                xOff,   // total offset in x
                yOff,   // total offset in y
                xyData = []; // two dimensional data array
            if (bonds[i].order === 1) {                 // single bond
                if (bonds[i].stereo === 1) {            // single wedge bond
                    var length = distance(x1, y1, x2, y2);
                    off = 0.1;
                    xOff = off * (y2 - y1) / length;
                    yOff = off * (x1 - x2) / length;
                    xyData = [
                        [x1, y1],
                        [x2 + xOff, y2 + yOff],
                        [x2 - xOff, y2 - yOff]
                    ];
                    graph.append('svg:path')
                        .style('fill', 'black')
                        .style('stroke-width', 1)
                        .attr('d', wedgeBond(xyData));
                } else if (bonds[i].stereo === 6) {     // single hash bond
                    off = 0.2;
                    xOff = off * (y2 - y1) / l;
                    yOff = off * (x1 - x2) / l;
                    var dxx1 = x2 + xOff - x1,
                        dyy1 = y2 + yOff - y1,
                        dxx2 = x2 - xOff - x1,
                        dyy2 = y2 - yOff - y1;
                    for (var j = 0.05; j <= 1; j += 0.15) {
                        xyData.push(
                            [x1 + dxx1 * j, y1 + dyy1 * j],
                            [x1 + dxx2 * j, y1 + dyy2 * j]
                            );
                    }

                    graph.append('svg:path')
                        .style('fill', 'none')
                        .style('stroke-width', 1)
                        .attr('d', hashBond(xyData))
                        .attr('stroke', 'black');
                } else if (bonds[i].stereo === 4) {     // single wiggly bond
                    off = 0.2;
                    xOff = off * (y2 - y1) / l;
                    yOff = off * (x1 - x2) / l;
                    var dxx1 = x2 + xOff - x1,
                        dyy1 = y2 + yOff - y1,
                        dxx2 = x2 - xOff - x1,
                        dyy2 = y2 - yOff - y1;
                    for (var j = 0.05; j <= 1; j += 0.1) {
                        if (xyData.length % 2 === 0) {
                            xyData.push(
                                [x1 + dxx1 * j, y1 + dyy1 * j]
                                );
                        } else {
                            xyData.push(
                                [x1 + dxx2 * j, y1 + dyy2 * j]
                                );
                        }
                    }

                    graph.append('svg:path')
                        .attr('d', wigglyBond(xyData))
                        .attr('fill', 'none')
                        .style('stroke-width', 1)
                        .attr('stroke', 'black');
                } else {                                // single plain bond
                    xyData = [
                        [x1, y1], [x2, y2]
                    ];
                    graph.append('svg:path')
                        .attr('d', plainBond(xyData))
                        .attr('stroke-width', '1')
                        .attr('stroke-linecap', 'round')
                        .attr('stroke-linejoin', 'round')
                        .attr('stroke', 'black');
                }
            } else if (bonds[i].order === 2) {          // double bond
                off = 0.1;
                xOff = off * (y2 - y1) / l;
                yOff = off * (x1 - x2) / l;
                xyData = [
                    [x1 + xOff, y1 + yOff], [x2 + xOff, y2 + yOff],
                    [x1 - xOff, y1 - yOff], [x2 - xOff, y2 - yOff]
                ];
                graph.append('svg:path').attr('d', plainBond(xyData))
                    .attr('stroke-width', '1')
                    .style('fill', 'none')
                    .attr('stroke-linecap', 'round')
                    .attr('stroke-linejoin', 'round')
                    .attr('stroke', 'black');
            } else if (bonds[i].order === 3) {          // triple bond
                off = 0.15;
                xOff = off * (y2 - y1) / l;
                yOff = off * (x1 - x2) / l;
                xyData = [
                    [x1, y1], [x2, y2],
                    [x1 + xOff, y1 + yOff], [x2 + xOff, y2 + yOff],
                    [x1 - xOff, y1 - yOff], [x2 - xOff, y2 - yOff]
                ];
                graph.append('svg:path')
                    .attr('d', plainBond(xyData))
                    .attr('stroke-width', '1')
                    .attr('stroke-linecap', 'round')
                    .attr('stroke-linejoin', 'round')
                    .attr('stroke', 'black');
            }
        }
        avgL /= bonds.length; // get average bond length
    };

    /**
     * Draws the atoms onto the SVG element. Note that the atoms are drawn
     * on top of the bonds.
     * 
     * @param {object[]} atoms An array of atom objects
     * @param {number} avgL An average bond length
     * @param {object} graph A SVG element
     */
    var drawAtoms = function (atoms, avgL, graph) {
        for (var i = 0; i < atoms.length; i++) {
            var atom = atoms[i];
            var atomCol = d3.rgb(atomColor[atom.symbol]);
            var g = graph.append('svg:g')
                .attr('transform', 'translate(' + 
                    x(atom.x) + ',' + y(atom.y) + ')');
            // draw a circle underneath the text
            g.append('svg:circle')
                // hack: magic number for scaling
                .attr('r', Math.ceil(avgL / 3))
                .attr('fill', 'white')
                .attr('opacity', '1');
            // draw the text string
            g.append('text')                                
                // hack: magic number for scaling
                .attr('dy', Math.ceil(avgL / 4.5))          
                .attr('text-anchor', 'middle')
                .attr('font-family', 'sans-serif')
                // hack: magic number for scaling
                .attr('font-size', Math.ceil(avgL / 1.5))   
                .attr('fill', atomCol)
                .text(atom.symbol);

            if (atom.charge !== 0) {
                var c = atom.charge;
                if (c < 0) {
                    c = (c === -1) ? '-' : (c + '-');
                } else {
                    c = (c === +1) ? '+' : (c + '+');
                }
                g.append('text')
                    .attr('dx', +1 * Math.ceil(avgL / 3))
                    .attr('dy', -1 * Math.ceil(avgL / 4.5))
                    .attr('text-anchor', 'left')
                    .attr('font-family', 'sans-serif')
                    // hack: magic number for scaling (half of symbol size)
                    .attr('fill', atomCol)
                    .attr('font-size', Math.ceil(avgL / 3)) 
                    .text(c);
            }

            if (atom.mass !== 0) {
                g.append('text')
                    .attr('dx', -2 * Math.ceil(avgL / 3))
                    .attr('dy', -1 * Math.ceil(avgL / 4.5))
                    .attr('text-anchor', 'left')
                    .attr('font-family', 'sans-serif')
                    // hack: magic number for scaling (half of symbol size)
                    .attr('font-size', Math.ceil(avgL / 3)) 
                    .attr('fill', atomCol)
                    .text(atom.mass);
            }
        }
    };

    /**
     * Calculates the Euclidean distance between two points.
     * 
     * @param {number} x1 A x value of first point
     * @param {number} y1 A y value of first point
     * @param {number} x2 A x value of second point
     * @param {number} y2 A y value of second point
     * @returns {number} the Euclidean distance
     */
    var distance = function (x1, y1, x2, y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    };

    /**
     * d3 line function using the SVG path mini language to draw a plain bond.
     */
    var plainBond = d3.svg.line()
        .interpolate(function (points) {
            var path = points[0][0] + ',' + points[0][1];
            for (var i = 1; i < points.length; i++) {
                if (i % 2 === 0) {
                    path += 'M' + points[i][0] + ',' + points[i][1];
                } else {
                    path += 'L' + points[i][0] + ',' + points[i][1];
                }
            }
            return path;
        })
        .x(function (d) {
            return x(d[0]);
        })
        .y(function (d) {
            return y(d[1]);
        });

    /**
     * d3 line function using the SVG path mini language to draw a wedge bond.
     */
    var wedgeBond = d3.svg.line()
        .x(function (d) {
            return x(d[0]);
        })
        .y(function (d) {
            return y(d[1]);
        });

    /**
     * d3 line function using the SVG path mini language to draw a hash bond.
     */
    var hashBond = d3.svg.line()
        .interpolate(function (points) {
            var path = points[0][0] + ',' + points[0][1];
            for (var i = 1; i < points.length; i++) {
                if (i % 2 === 0) {
                    path += 'M' + points[i][0] + ',' + points[i][1];
                } else {
                    path += 'L' + points[i][0] + ',' + points[i][1];
                }
            }
            return path;
        })
        .x(function (d) {
            return x(d[0]);
        })
        .y(function (d) {
            return y(d[1]);
        });

    /**
     * d3 line function using the SVG path mini language to draw a wiggly bond.
     */
    var wigglyBond = d3.svg.line()
        .interpolate('cardinal')
        .x(function (d) {
            return x(d[0]);
        })
        .y(function (d) {
            return y(d[1]);
        });

    /*
     * Atom properties containing the CPK color values.
     */
    var atomColor = {
        H: '#000000',
        He: '#FFC0CB',
        Li: '#B22222',
        B: '#00FF00',
        C: '#000000',
        N: '#8F8FFF',
        O: '#F00000',
        F: '#DAA520',
        Na: '#0000FF',
        Mg: '#228B22',
        Al: '#808090',
        Si: '#DAA520',
        P: '#FFA500',
        S: '#FFC832',
        Cl: '#00FF00',
        Ca: '#808090',
        Ti: '#808090',
        Cr: '#808090',
        Mn: '#808090',
        Fe: '#FFA500',
        Ni: '#A52A2A',
        Cu: '#A52A2A',
        Zn: '#A52A2A',
        Br: '#A52A2A',
        Ag: '#808090',
        I: '#A020F0',
        Ba: '#FFA500',
        Au: '#DAA520'
    };

    // reference visible (public) functions as properties
    return {
        draw: draw
    };
};

/**
 * Helper function to create divs for the spinner animation (defined in css).
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @param {string} el An element identifier to append the spinner to
 * @return {object} the spinner element
 */
st.util.spinner = function (el) {
    if ($('.st-spinner').length) { // singleton
        return $('.st-spinner');
    }
    // append the sub-divs to the spinner element
    $(el).append('<div class="st-spinner">' +
        '<div class="st-bounce1"></div>' + 
        '<div class="st-bounce2"></div>' +
        '<div class="st-bounce3"></div>' +
        '</div>');
        
    return $('.st-spinner');
};

/**
 * Builds a compare function to sort an array of objects.
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @param {string} xacc An x value accessor
 * @return {object} the compare function
 */
st.util.compare = function (xacc) {
    var compare = function (a, b) {
        if (a[xacc] < b[xacc]) {
            return -1;
        }
        if (a[xacc] > b[xacc]) {
            return 1;
        }
        return 0;
    };
    return compare;
};
/**
 * Enum for annotation types.
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @enum {string}
 */
st.annotation = {
    TOOLTIP: 'tooltip',         // tooltip text, plain text key value pairs
    TOOLTIP_MOL: 'tooltip_mol', // tooltip molecule, resolves URLs to SDfiles
    ANNOTATION: 'annotation',   // canvas annotation, drawn onto the canvas
    ANNOTATION_COLOR: 'annotation_color' // canvas annotation color
};
/**
 * parser stub.
 *
 * Parsers for input data should extend this stub.
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 */
st.parser = {};

/**
 * Incomplete rudimentary JCAMP-DX parser for PAC compressed files and 
 * arrays of type ##XYDATA= (X++(Y..Y)).
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @deprecated
 * @param {string} url A url to the JCAMP-DX file
 * @param {function} callback A callback function
 */
st.parser.jdx = function (url, callback) {
    // d3 AJAX request to resolve the URL
    d3.text(url, function (jdx) {
        // essential key definitions
        var LABEL = '##',
            END = 'END',
            XYDATA = 'XYDATA',
            YTABLE = '(X++(Y..Y))',
            //XFACTOR = 'XFACTOR',
            YFACTOR = 'YFACTOR',
            FIRSTX = 'FIRSTX',
            LASTX = 'LASTX';
            //NPOINTS = 'NPOINTS';
        
        // the data store
        var objs = [];
        // tmp helper objects
        var obj = {},
            data = false,
            points = [];
        // tmp helper objects
        var pair,
            key,
            pkey,
            value;
    
        // split input text into separate lines
        var lines = jdx.split(/\r\n|\r|\n/g);
        // iterate over all lines
        for (var i in lines) {
            var line = lines[i];
            if (line.indexOf(LABEL) === 0) {
                pair = line.split(/=\s(.*)/); // split key-value pair
                if (pair.length < 2) {        // sanity check
                    continue;
                }
                key = pair[0].slice(2);                     // parse key
                value = pair[1].split(/\$\$(.*)/)[0].trim();// parse value
                if (key === XYDATA && value === YTABLE) {
                    data = true; // boolean flag whether this is a data table
                } else if (key === END) {
                    if (data) {  // clean up after a data table has been parsed
                        if (parseFloat(obj[FIRSTX]) > 
                            parseFloat(obj[LASTX])) {
                            points.reverse();
                        }
                        obj[pkey] = points;
                        objs.push(obj);
                        // reset
                        obj = {};
                        data = false;
                        points = [];
                    }
                    data = false;
                } else {
                    obj[key] = value;
                }
                pkey = key;
            } else if (data) {
                //var deltax = (obj[LASTX] - obj[FIRSTX]) / (obj[NPOINTS] - 1);
                var entries = line.match(/(\+|-)*\d+\.*\d*/g);
                //var x = obj[XFACTOR] * entries[0];
                for (var j = 1; j < entries.length; j++) {
                    //x += (j - 1) * deltax;
                    var y = obj[YFACTOR] * entries[j];
                    points.push(y);
                }
            }
        }
        callback(objs);
    });
};
/**
 * Default data object. Custom data objects should extend this data stub. 
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @returns {object} the default data object
 */
st.data = {};

/**
 * Builds the default data object that serves as base for custom data objects.
 * 
 * @constructor
 * @returns {object} the default data object
 */
function data () {
    return {
        opts: { // data options
            title: '',
            src: [],    // JSON URLs or JSON data
            anno: [],   // JSON URLs or JSON data
            x: 'x',     // x accessor
            y: 'y',     // y accessor
            xlimits: [],// x axis limits: min, max
            ylimits: [],// y axis limits: min, max
            annoTypes: [],  // annotation types (see st.annotation)
            annoTexts: []   // annotation titles (string)
        },
        
        raw: {          // global variables summarising the data set
            gxlim: [ Number.MAX_VALUE, Number.MIN_VALUE], // global x limits
            gylim: [ Number.MAX_VALUE, Number.MIN_VALUE], // global y limits
            ids: {},    // identifier hash set of all series in the data set
            series: [], // all series in the data set (array of series)
            minima: 0,      // whether minimum binned is to be applied 
            annoGroups: {}  // annotation groups (string)
        },
        
        /**
         * Sets the title accessor.
         *
         * @param {string} x A title accessor
         * @returns {object} the data object
         */
        title: function (x) {
            if (x && typeof x === 'string') {
                this.opts.title = x;
            } else {
                console.log('Invalid title option.');
            }
            return this;
        },
        
        /**
         * Sets the y accessor.
         *
         * @param {string} y A y data accessor
         * @returns {object} the data object
         */
        y: function (y) {
            if (y && typeof y === 'string') {
                this.opts.y = y;
            } else {
                console.log('Invalid y accessor option.');
            }
            return this;
        },
        
        /**
         * Sets the x domain limits.
         *
         * @param {number[]} limits A two element array of min and max limits
         * @returns {object} the data object
         */
        xlimits: function (x) {
            if (x && x instanceof Array) {
                this.opts.xlimits = x;
            } else {
                console.log('Invalid x domain limits.');
            }
            return this;
        },
        
        /**
         * Sets the y domain limits.
         *
         * @param {number[]} limits A two element array of min and max limits
         * @returns the data object
         */
        ylimits: function (x) {
            if (x && x instanceof Array) {
                this.opts.ylimits = x;
            } else {
                console.log('Invalid y domain limits.');
            }
            return this;
        },
            
        /**
         * Sets the data source option.
         *
         * @param {string|string[]} datarefs An URL (array) or JSON data (array)
         * @param {string|string[]} annorefs An URL (array) or JSON data (array)
         * @returns {object} the data object
         */
        add: function (datarefs, annorefs) {
            if (datarefs instanceof Array) {
                if (!annorefs || annorefs instanceof Array) {
                    this.opts.src.push.apply(this.opts.src, datarefs);
                    this.opts.anno.push.apply(this.opts.anno, annorefs);
                } else {
                    console.log('Raw data and annotation data must be ' +
                        'of the same type.');
                }
            } else {
                this.opts.src.push(datarefs);
                this.opts.anno.push(annorefs);
            }
        },
        
        /**
         * Defines elements of the annotation data structure.
         *
         * @param {string} type Type of st.annotation
         * @param {string} text Title of the annotation
         */
        annotationColumn: function (type, text) {
            if (type.toUpperCase() in st.annotation) {
                this.opts.annoTypes.push(type);
                this.opts.annoTexts.push(text);
            } else {
                console.log('Unknown annotation type: ' + type);
            }
        },

        /**
         * Removes a data series by its identifier or index.
         *
         * @param {string[]|number[]} x The indices or identifiers to remove
         * @returns {string[]} an array of removed identifiers
         */
        remove: function (x) {
            // array to collect identifiers of removed series
            var ids = [];
                   
            // if no argument is given, clear the chart
            if (!x && x !== 0) {
                // collect all identifiers
                for (var i in this.raw.ids) {
                    ids.push(i);
                }
                
                // reset the identifier set and the global 'raw' container
                this.raw.ids = {};
                this.raw.series = [];
                this.raw.gxlim = [ Number.MAX_VALUE, Number.MIN_VALUE];
                this.raw.gylim = [ Number.MAX_VALUE, Number.MIN_VALUE];
                
                // return the collected identifiers
                return ids;
            }
            
            // turn a single identifier into an array of identifiers
            if (!(x instanceof Array)) {
                x = [ x ];
            } else {
                x.sort();
            }
            
            // iterate over the array of identifiers to remove
            for (i in x) {
                var xid = x[i];
                // check whether the identifier is a string...
                if (isNaN(xid)) {
                    // find the identifier in the data set and delete it
                    for (var i in this.raw.series) {
                        if (this.raw.series[i].id === xid) {
                            this.raw.series.splice(i, 1);
                            ids.push(this.raw.ids[xid]);
                            delete this.raw.ids[xid];
                            break;
                        }
                    }
                // ...or a number, in which case its an index
                } else {
                    // sanity check for the index (track removed entries)
                    if (xid - i < this.raw.series.length) {
                        var spliced = this.raw.series.splice(xid - i, 1);
                        ids.push(spliced[0].id);
                        delete this.raw.ids[spliced[0].id];
                    }
                }
            }
            // reset the global domain limits
            if (this.raw.series.length === 0) {
                this.raw.gxlim = [ Number.MAX_VALUE, Number.MIN_VALUE];
                this.raw.gylim = [ Number.MAX_VALUE, Number.MIN_VALUE];
            }
            
            // return the collected identifiers
            return ids;
        },
        
        /**
         * Gets the id of a data series at a given index.
         *
         * @param {number} index A data series index 
         * @returns {string} the identifier of the data series
         */
        id: function (index) {
            return this.raw.series[index].id;
        },
        
        /**
         * Gets the title of a data series at a given index.
         *
         * @param {number} index A data series index 
         * @returns {string} the title of the data series
         */
        titleat: function (index) {
            return this.raw.series[index].title;
        },
        
        /**
         * Gets the x and y accessors for a data series at a given index.
         *
         * @param {number} index A data series index 
         * @returns the x and y accessors of the data series`
         */
        accs: function (index) {
            return this.raw.series[index].accs;
        },
        
        /**
         * Pushes the source values currently in the source option into 
         * the raw data array and sets the global data options.
         *
         * @param {function} callback A callback function
         */
        push: function (callback) {
            // self-reference for nested functions
            var data = this;
            // array for XHR promises
            var deferreds = [];
            // iterate over the source values
            for (var i in this.opts.src) {
                // check whether source value is a data object
                // the corresponding annotation reference is assumed to be a 
                // data object as well in that case
                if (typeof this.opts.src[i] !== 'string') {
                    this.fetch(this.opts.src[i], this.opts.anno[i]);
                } else { // resolve the URLs and save the promises
                    deferreds.push(this.fetch(
                        this.opts.src[i], this.opts.anno[i]));
                }
            }
            // wait until all promises are fulfilled
            $.when.apply($, deferreds).done(function () {
                if (!data.opts.src.length) {
                    return;
                }
                
                // clear the source buffers
                data.opts.src = [];
                data.opts.anno = [];
                
                // special case: single value data sets:
                // expand X and Y range by 1%
                if (data.raw.gxlim[0] === data.raw.gxlim[1]) {
                    data.raw.gxlim[0] -= data.raw.gxlim[0] / 100.0;
                    data.raw.gxlim[1] += data.raw.gxlim[1] / 100.0;
                }
                if (data.raw.gylim[0] === data.raw.gylim[1]) {
                    data.raw.gylim[0] -= data.raw.gylim[0] / 100.0;
                    data.raw.gylim[1] += data.raw.gylim[1] / 100.0;
                }
                callback();
            });
        },
        
        /**
         * Fetches the data series and adds it as raw entry.
         *
         * @param {string|object} src A data source
         * @param {string|object} anno An annotation source
         */
        fetch: function (src, anno) {
            // self-reference for nested functions
            var set = this;
            // the XHR promise
            var jqxhr = null;
            // 1) input series referenced by a URL
            if (typeof src === 'string') {
                // 1a) input annotations referenced by a URL
                if (typeof anno === 'string' && anno) {
                    jqxhr = $.when(
                        $.get(src),
                        $.get(anno)
                    )
                    .fail(function() {
                        console.log('Fetch failed for: ' + src + '\n' + anno);
                    })
                    .then(function(json, json2) {
                        if (typeof json === 'string') {
                            json = $.parseJSON(json);
                        }
                        // assumption: series and anno structure are identical
                        if (json[0] instanceof Array) {
                            for (var i in json[0]) {
                                set.seriesfetch(json[0][i], json2[0][i]);
                            }
                        } else {
                            set.seriesfetch(json[0], json2[0]);
                        }
                    });
                // 1b) input annotations provided as data array (or missing)
                } else {
                    jqxhr = $.when(
                        $.get(src)
                    )
                    .fail(function() {
                        console.log('Fetch failed for: ' + src);
                    })
                    .then(function(json) {
                        if (typeof json === 'string') {
                            json = $.parseJSON(json);
                        }
                        // assumption: series and anno structure are identical
                        if (json instanceof Array) {
                            if (!anno) {
                                anno = [];
                            }
                            for (var i in json) {
                                set.seriesfetch(json[i], anno[i]);
                            }
                        } else {
                            set.seriesfetch(json, anno);
                        }
                    });
                }
            // 2) input series provided as data array
            } else {
                // 2a) input annotations referenced by a URL
                if (typeof anno === 'string' && anno) {
                    jqxhr = $.when(
                        $.get(anno)
                    )
                    .fail(function() {
                        console.log('Fetch failed for: ' + anno);
                    })
                    .then(function(json) {
                        // assumption: series and anno structure are identical
                        if (src instanceof Array) {
                            if (!anno) {
                                anno = [];
                            }
                            for (var i in src) {
                                set.seriesfetch(src[i], json[i]);
                            }
                        } else {
                            set.seriesfetch(src, json);
                        }
                    });
                } else {
                    // 1b) input annotations provided as data array (or missing)
                    if (src instanceof Array) {
                        for (var i in src) {
                            set.seriesfetch(src[i], anno[i]);
                        }
                    } else {
                        set.seriesfetch(src, anno);
                    }
                }
            }
            return jqxhr;
        },
        
        /**
         * Resets all global options.
         */
        reset: function () {
            for (var i in this.raw.series) {
                var series = this.raw.series[i];
                series.size = [
                    0,
                    series.data.length,
                    0
                ];
            }
        }
    };
}

/**
 * Function resovling axis limits based on whether key values, numeric values,
 * or no input values are provided.
 * 
 * @params {object} series The data array
 * @params {object} json The complete data series
 * @params {number[]} limits The min/max array
 * @params {string} acc The data accessor
 * @returns {number[]} the axis min/max limts
 */
function fetch_limits (series, json, limits, acc) {
    var lim = [];
    // sanity check
    if (limits.length === 2) {
        // both variables are accessors
        if (isNaN(limits[0]) && isNaN(limits[1])) {
            lim = [
                json[limits[0]],
                json[limits[1]]
            ];
        // both variables are constants
        } else if (typeof limits[0] === 'number' 
                && typeof limits[1] === 'number') {
            lim = [
                limits[0],
                limits[1]
            ];
        } else {
            // typically a one dimensional array: search
            if (!acc || acc === '') {
                lim = d3.extent(series);
            // typically a set: search
            } else {
                lim = d3.extent(series, function (d) {
                    return d[acc];
                });
            }   
        }
    // sanity violation: search
    } else {
        if (!acc || acc === '') {
            lim = d3.extent(series);
        } else {
            lim = d3.extent(series, function (d) {
                return d[acc];
            });
        }
    }
    lim[0] = parseFloat(lim[0]);
    lim[1] = parseFloat(lim[1]);
    return lim;
}


/**
 * Model of a two dimensional data set with x and y values.
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @extends st.data.data
 * @returns {object} a data structure of type 'set'
 */
st.data.set = function () {
    // base data structure to be extended
    var set = data();
    
    /**
     * Sets the x data accessor.
     *
     * @param {string} x A x data accessor
     * @returns {object} the data object
     */
    set.x = function (x) {
        if (x && typeof x === 'string') {
            this.opts.x = x;
        } else {
            console.log('Invalid y accessor option.');
        }
        return this;
    };
    
    /**
     * Gets the unbinned data array for the current chart.
     *
     * @param {number} width The chart width
     * @param {function} xscale The d3 x axis scale
     * @returns {object[]} the unbinned data array
     */
    set.get = function (width, xscale) {
        // global data container for all series
        var rawbinned = [];
        // define domain extrema in x
        var ext = [
            xscale.invert(0),
            xscale.invert(width)
        ];
        // arrange based on x-axis direction
        ext = st.util.domain(xscale, ext);
        // iterate over all series
        for (var i in this.raw.series) {
            var series = this.raw.series[i];
            var binned = [];
            // iterate over the current series...
            for (var j in series.data) {
                var x = series.x(j);
                //...and select data points within the domain extrema
                if (x >= ext[0] && x <= ext[1]) {
                    binned.push(series.data[j]);
                }
            }
            // add the filtered series to the global data container
            rawbinned.push(binned);
        }
        // return the global data container
        return rawbinned;
    };
    
    /**
     * Gets the binned data array for the current chart.
     *
     * @param {number} width The chart width
     * @param {function} xscale The d3 x axis scale
     * @param {boolean} invert Whether to bin using min instead of max
     * @returns {object[]} the binned data array
     */
    set.bin = function (width, xscale, invert) {
        // global data container for all series
        var rawbinned = [];
        // define domain extrema in x
        var ext = [
            xscale.invert(0),
            xscale.invert(width)
        ];
        // arrange based on x-axis direction
        ext = st.util.domain(xscale, ext);
        // define bin width in px
        var binWidth = 1
        
        // find global max number of bins
        var gnbins = 0;
        // iterate over all series
        for (var i in this.raw.series) {
            // get the series
            var tmp = this.raw.series[i].size;
            // if (tmp[2] === 0) { // whether nbins is already initialised
                tmp[2] = Math.ceil(width / binWidth);
            // }
            // check if tmp nbins is greather than the current global nbins
            if (gnbins < tmp[2]) {
                gnbins = tmp[2];
            }
        }
        
        // calculate the bin step size
        var step = Math.abs(ext[1] - ext[0]) / (gnbins - 1);
        
        // iterate over all series
        for (var i in this.raw.series) {
            // get the series
            var series = this.raw.series[i];
            // get the number of data points in this series
            var serieslength = series.data.length;
            // get the size array: [domain min, domain max, nbins]
            var tmp = series.size;
            // local data container for binned series
            var binned = [];
            // counter to shorten the data array if applicable
            var cor = 0;
            
            // reverse min limit to include unrendered data points if required
            while (tmp[0] > 0) {
                var x = series.x(tmp[0]);
                if (x < ext[0]) {
                    break;
                }
                tmp[0] -= 1;
            }
            // forward max limit to include unrendered data points if required
            while (tmp[1] < serieslength) {
                var x = series.x(tmp[1]);
                if (x > ext[1]) {
                    break;
                }
                tmp[1] += 1;
            }
            
            // iterate over all data points within the min/max domain limits
            for (var j = tmp[0]; j < tmp[1]; j++) {
                var x = series.x(j);
                // skip irrelevant data points
                if (x < ext[0]) {
                    tmp[0] = j;
                    continue;
                } else if (x > ext[1]) {
                    tmp[1] = j;
                    break;
                }
                
                // get the target bin
                var bin = Math.floor((x - ext[0]) / step);
                // get the current data point in the bin
                var dpb = binned[bin - cor];
                // get the data point to be added to the bin
                var dps = series.data[j];
                // if the bin is already populated with a data point...
                if (dpb) {
                    // a) ...bin by minimum
                    if (invert) {
                        if (dpb[series.accs[1]] < dps[series.accs[1]]) {
                            binned[bin - cor] = dpb;
                        } else {
                            if (dpb.annos && !dps.annos) {
                                dps.annos = dpb.annos;
                            }
                            binned[bin - cor] = dps;
                        }   
                    // b) ...bin by maximum
                    } else {
                        if (Math.abs(dpb[series.accs[1]]) > 
                            Math.abs(dps[series.accs[1]])) {
                            binned[bin - cor] = dpb;
                        } else {
                            if (dpb.annos && !dps.annos) {
                                dps.annos = dpb.annos;
                            }
                            binned[bin - cor] = dps;
                        }
                    }
                // ...add the current data point to the unpopulated bin
                } else {
                    cor = bin - binned.length;
                    binned[bin - cor] = dps;
                }
            }
            // correct the local nbins value if the array could be shortened
            if (cor > 0) {
                tmp[2] = binned.length;
            }
            // assign the current data size array to its series
            series.size = tmp;
            // add the binned array to the global container
            rawbinned.push(binned);
        }
        return rawbinned;
    };
    
    /**
     * Function parsing the input data (and annotations).
     *
     * @param {string[]} json The raw data series
     * @param {string[]} json2 The raw annotation data
     * @param {object} set The target data set
     */
    set.seriesfetch = function (json, json2) {
        var id = st.util.hashcode((new Date().getTime() * Math.random()) + '');
        id = 'st' + id;                     // series id
        var title = json[set.opts.title];   // series title
        var xlim = [];                  // series x limits
        var ylim = [];                  // series y limits
        var size = [];                  // series size: min, max, nBins
        var xacc = this.opts.x;          // series x accessor
        var yacc = this.opts.y;          // series y accessor
        
        if (!title || title.length === 0) {
            title = id;
        }
        
        if (id in this.raw.ids) {
            console.log("SpeckTackle: Non unique identifier: " + id);
            return;
        }
        
        var acc = ''; // resolve accessor stub
        if (xacc.lastIndexOf('.') !== -1) {
            acc = xacc.substr(0, xacc.lastIndexOf('.'))
            xacc = xacc.substr(xacc.lastIndexOf('.') + 1)
            yacc = yacc.substr(yacc.lastIndexOf('.') + 1)
        }

        // coerce two arrays into an array of objects 
        var data = (acc === '') ? json : json[acc];
        if (!(data instanceof Array)) {
            var grouped = [];
            for (var i in data[xacc]) {
                var ob = {};
                ob[xacc] = data[xacc][i];
                ob[yacc] = data[yacc][i];
                grouped.push(ob);
            }
            data = grouped;
        }
        
        
        // resolve limits
        xlim = fetch_limits(data, json, this.opts.xlimits, xacc);
        ylim = fetch_limits(data, json, this.opts.ylimits, yacc);
        size = [0, data.length, 0];
        
        // assign annotations
        if (json2) {
            // sort the data set
            data.sort(st.util.compare(xacc));
            // define bisector for value lookup
            var bisector = d3.bisector(function (d) {
                return d[xacc];
            }).left;
            var annolength = this.opts.annoTypes.length;
            // iterate over each annotation record
            for (var i in json2) {
                // ignore annotation record if of invalid length
                if (json2[i].length - 2 !== annolength) {
                    continue;
                }
                // get the annotation group
                var refgroup = json2[i][0];
                if (!(refgroup in this.raw.annoGroups)) {
                    this.raw.annoGroups[refgroup] = 0;
                }
                // get the annotation reference value
                var ref = json2[i][1];
                // find the data point in the data series
                var refpos = bisector(data, ref);
                if (refpos !== -1 && ref === data[refpos][xacc]) {
                    var refpoint = data[refpos];
                    // add annotation hash to the data point
                    if (!refpoint.annos) {
                        refpoint.annos = {};
                    }
                    // add group to the annotation hash
                    var dpannos = refpoint.annos;
                    if (!(refgroup in dpannos)) {
                        dpannos[refgroup] = {};
                    }
                    var annosgroup = dpannos[refgroup];
                    
                    // iterate over each element of the annotation record
                    for (var j = 0; j < annolength; j++) {
                        var reftype = this.opts.annoTypes[j];
                        var val = json2[i][j + 2];
                        if (reftype === st.annotation.ANNOTATION) {
                            annosgroup.annotation = val;
                        } else if (reftype === st.annotation.TOOLTIP) {
                            if (!annosgroup.tooltip) {
                                annosgroup.tooltip = {};
                            }
                            annosgroup.tooltip[this.opts.annoTexts[j]] = val;
                        } else if (reftype === st.annotation.TOOLTIP_MOL) {
                            if (val !== '') {
                                if (!annosgroup.tooltipmol) {
                                    annosgroup.tooltipmol = {};
                                }
                                annosgroup.tooltipmol[
                                    this.opts.annoTexts[j]] = val;
                            }
                        }
                    }
                }
            }
        }
        
        // replace global limits if required
        if (xlim[0] < this.raw.gxlim[0]) {
            this.raw.gxlim[0] = xlim[0];
        }
        if (ylim[0] < this.raw.gylim[0]) {
            this.raw.gylim[0] = ylim[0];
        }
        if (xlim[1] > this.raw.gxlim[1]) {
            this.raw.gxlim[1] = xlim[1];
        }
        if (ylim[1] > this.raw.gylim[1]) {
            this.raw.gylim[1] = ylim[1];
        }                
        
        this.raw.ids[id] = true;
        
        // add series as raw entry
        this.raw.series.push({
            id: id,
            title: title,
            xlim: xlim,
            ylim: ylim,
            accs: [xacc, yacc],
            size: size,
            data: data,
            x: function (i) { // x accessor function
                return this.data[i][this.accs[0]];
            },
            y: function (i) {   // y accessor function
                return this.data[i][this.accs[1]];
            }
        });
    };
    
    return set;
};

/**
 * Model of a one dimensional data array with y values.
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @extends st.data.data
 * @returns {object} a data structure of type 'set'
 */
st.data.array = function () {
    // base data structure to be extended
    var array = data();
    
    /**
     * Gets the unbinned data array for the current chart.
     *
     * @param {number} width The chart width
     * @param {function} xscale The d3 x axis scale
     * @returns {object[]} the unbinned data array
     */
    array.get = function (width, xscale) {
        // global data container for all series
        var rawbinned = [];
        // define domain extrema in x
        var ext = [
            xscale.invert(0),
            xscale.invert(width)
        ];
        // arrange based on x-axis direction
        ext = st.util.domain(xscale, ext);
        // iterate over all series
        for (var i in this.raw.series) {
            // get the current series
            var series = this.raw.series[i];
            // get the number of data points
            var serieslength = series.data.length;
            // calculate the step size in x for the series
            var seriesstep = (series.xlim[1] - series.xlim[0]) / serieslength;
            // get the size array: [domain min, domain max, nbins]
            var tmp = series.size;
            // local data container for binned series
            var binned = [];
            
            // reverse min limit to include unrendered data points if required
            while (series.size[0] > 0) {
                var x = series.size[0] * seriesstep + series.xlim[0];
                if (x < ext[0]) {
                    break;
                }
                series.size[0] -= 1;
            }
            // forward max limit to include unrendered data points if required
            while (series.size[1] < serieslength) {
                var x = series.size[1] * seriesstep + series.xlim[0];
                if (x > ext[1]) {
                    break;
                }
                series.size[1] += 1;
            }
            
            // iterate over all data points
            for (var j = series.size[0]; j < series.size[1]; j++) {
                // calculate the x value for the current index
                var x = j * seriesstep + series.xlim[0];
                // skip irrelevant data points
                if (x < ext[0]) {
                    tmp[0] = j;
                    continue;
                } else if (x > ext[1]) {
                    tmp[1] = j;
                    break;
                }
                
                // get the current y value
                var ys = series.data[j];
                // build the data point
                var dp = {
                    x: x
                };
                dp[series.accs[1]] = ys;
                binned.push(dp);
            }
            // assign the current data size array to its series
            series.size = tmp;
            // add the unbinned array to the global container
            rawbinned.push(binned);
        }
        return rawbinned;
    };
    
    /**
     * Gets the binned data array for the current chart.
     *
     * @param {number} width The chart width
     * @param {function} xscale The d3 x axis scale
     * @param {boolean} invert Whether to bin using min instead of max
     * @returns {object[]} the binned data array
     */
    array.bin = function (width, xscale, invert) {
        // global data container for all series
        var rawbinned = [];
        // define domain extrema in x
        var ext = [
            xscale.invert(0),
            xscale.invert(width)
        ];
        // arrange based on x-axis direction
        ext = st.util.domain(xscale, ext);
        // define bin width in px
        var binWidth = 1;
        
        // find global max number of bins
        var gnbins = 0;
        // iterate over all series
        for (var i in this.raw.series) {
            // get the series
            var tmp = this.raw.series[i].size;
            // if (tmp[2] === 0) { // whether nbins is already initialised
                tmp[2] = Math.ceil(width / binWidth);
            // }
            // check if tmp nbins is greather than the current global nbins
            if (gnbins < tmp[2]) {
                gnbins = tmp[2];
            }
        }
        
        // calculate the bin step size
        var step = Math.abs(ext[1] - ext[0]) / (gnbins - 1);
        
        // iterate over all series
        for (var i in this.raw.series) {
            // get the series
            var series = this.raw.series[i];
            // get the number of data points in this series
            var serieslength = series.data.length;
            // calculate the step size in x for the series
            var seriesstep = (series.xlim[1] - series.xlim[0]) / serieslength;
            // get the size array: [domain min, domain max, nbins]
            var tmp = series.size;
            // local data container for binned series
            var binned = [];
            // counter to shorten the data array if applicable
            var cor = 0;
            
            // reverse min limit to include unrendered data points if required
            while (series.size[0] > 0) {
                var x = series.size[0] * seriesstep + series.xlim[0];
                if (x < ext[0]) {
                    break;
                }
                series.size[0] -= 1;
            }
            // forward max limit to include unrendered data points if required
            while (series.size[1] < serieslength) {
                var x = series.size[1] * seriesstep + series.xlim[0];
                if (x > ext[1]) {
                    break;
                }
                series.size[1] += 1;
            }
            
            // iterate over all data points
            for (var j = series.size[0]; j < series.size[1]; j++) {
                // calculate the x value for the current index
                var x = j * seriesstep + series.xlim[0];
                // skip irrelevant data points
                if (x < ext[0]) {
                    tmp[0] = j;
                    continue;
                } else if (x > ext[1]) {
                    tmp[1] = j;
                    break;
                }
                
                // get the target bin
                var bin = Math.floor((x - ext[0]) / step);
                // get the current data point in the bin
                var dpb = binned[bin - cor];
                // get the data point to be added to the bin
                var ys = series.data[j];
                // if the bin is already populated with a data point...
                if (dpb) {
                    // a) ...bin by minimum
                    if (invert) {
                        if (dpb[series.accs[1]] < ys) {
                            binned[bin - cor] = dpb;
                        } else {
                            var dp = { 
                                x: x
                            };
                            dp[series.accs[1]] = ys;
                            var tmpdp = binned[bin - cor];
                            if (tmpdp.annos) {
                                dp.annos = binned[bin - cor].annos;
                            }
                            binned[bin - cor] = dp;
                        }
                    // b) ...bin by maximum
                    } else {
                        if (Math.abs(dpb[series.accs[1]]) > Math.abs(ys)) {
                            binned[bin - cor] = dpb;
                        } else {
                            var dp = { 
                                x: x
                            };
                            dp[series.accs[1]] = ys;
                            var tmpdp = binned[bin - cor];
                            if (tmpdp.annos) {
                                dp.annos = tmpdp.annos;
                            }
                            binned[bin - cor] = dp;
                        }
                    }
                // ...add the current data point to the unpopulated bin
                } else {
                    cor = bin - binned.length;
                    binned[bin - cor] = { 
                            x: x
                    };
                    binned[bin - cor][series.accs[1]] = ys;
                }
                
                // assign annotations
                if (series.annos && Object.keys(series.annos).length) {
                    if (j in series.annos && !binned[bin - cor].annos) {
                        var refpoint = binned[bin - cor];
                        var ref = series.annos[j];
                        // get the annotation group
                        var refgroup = ref[0];
                        if (!(refgroup in this.raw.annoGroups)) {
                            this.raw.annoGroups[refgroup] = 0;
                        }
                        // add annotation hash to the data point
                        if (!refpoint.annos) {
                            refpoint.annos = {};
                        }
                        // add group to the annotation hash
                        var dpannos = refpoint.annos;
                        if (!(refgroup in dpannos)) {
                            dpannos[refgroup] = {};
                        }
                        var annosgroup = dpannos[refgroup];
                        
                        // iterate over each element of the annotation record
                        for (var k = 0; k < ref.length; k++) {
                            var reftype = this.opts.annoTypes[k];
                            var val = ref[k + 2];
                            if (reftype === st.annotation.ANNOTATION) {
                                annosgroup.annotation = val;
                            } else if (reftype === st.annotation.TOOLTIP) {
                                if (!annosgroup.tooltip) {
                                    annosgroup.tooltip = {};
                                }
                                annosgroup.tooltip[this.opts.annoTexts[k]] = val;
                            } else if (reftype === st.annotation.TOOLTIP_MOL) {
                                if (val !== '') {
                                    if (!annosgroup.tooltipmol) {
                                        annosgroup.tooltipmol = {};
                                    }
                                    annosgroup.tooltipmol[
                                        this.opts.annoTexts[k]] = val;
                                }
                            }
                        }
                        binned[bin - cor] = refpoint;
                    }
                }
            }
            // correct the local nbins value if the array could be shortened
            if (cor > 0) {
                tmp[2] = binned.length;
            }
            // assign the current data size array to its series
            series.size = tmp;
            // add the binned array to the global container
            rawbinned.push(binned);
        }
        return rawbinned;
    };
    
    /**
     * Function parsing the input data (and annotations).
     *
     * @param {string[]} json The raw data series
     * @param {string[]} json2 The raw annotation data
     * @param {object} set The target data set
     */
    array.seriesfetch = function (json, json2) {
        var id = st.util.hashcode((new Date().getTime() * Math.random()) + '');
        id = 'st' + id;                       // model id
        var title = json[this.opts.title];   // model title
        var xlim = [];                  // model x limits
        var ylim = [];                  // model y limits
        var size = [];                  // model size: min, max, nBins
        var xacc = 'x';                 // model x accessor
        var yacc = this.opts.y;        // model y accessor

        if (!title || title.length === 0) {
            title = id;
        }
        
        if (id in this.raw.ids) {
            console.log("SpeckTackle: Non unique identifier: " + id);
            return;
        }
        
        var data = (yacc === '') ? json : json[yacc]; // resolve accessor stub
        // resolve limits
        xlim = fetch_limits(data, json, this.opts.xlimits, xacc);
        ylim = fetch_limits(data, json, this.opts.ylimits);
        size = [0, data.length, 0];
        
        // assign annotations
        var annos = {};
        if (json2) {
            var annolength = this.opts.annoTypes.length;
            // iterate over each annotation record
            for (var i in json2) {
                // ignore annotation record if of invalid length
                if (json2[i].length - 2 !== annolength) {
                    continue;
                }
                // get the annotation reference index
                var refpos = json2[i][1];
                if (refpos < size[1]) {
                    annos[refpos] = json2[i];
                }
            }
        }
        
        // replace global limits if required
        if (xlim[0] < this.raw.gxlim[0]) {
            this.raw.gxlim[0] = xlim[0];
        }
        if (ylim[0] < this.raw.gylim[0]) {
            this.raw.gylim[0] = ylim[0];
        }
        if (xlim[1] > this.raw.gxlim[1]) {
            this.raw.gxlim[1] = xlim[1];
        }
        if (ylim[1] > this.raw.gylim[1]) {
            this.raw.gylim[1] = ylim[1];
        }

        this.raw.ids[id] = true;    

        // add series as raw entry
        this.raw.series.push({
            id: id,        
            title: title,
            xlim: xlim,
            ylim: ylim,
            accs: [xacc, yacc],
            size: size,
            annos: annos,
            data: data,
            x: function (i) {
                return i; // return i by default
            },
            y: function (i) {
                return this.data[i][this.accs[1]];
            }
        });
    };
    
    return array;
};

/**
 * Base chart to be extended by custom charts.
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @returns {object} the base chart
 */
st.chart = {};

/**
 * Builds the base chart object that serves as base for custom charts.
 * 
 * @constructor
 * @returns {object} the base chart
 */
function chart () {
    return {
        opts: { // chart options
            title: '',          // chart title
            xlabel: '',         // chart x-axis label
            ylabel: '',         // chart y-axis label
            xreverse: false,    // whether to reverse the x-axis
            yreverse: false,    // whether to reverse the y-axis
            legend: false,      // whether to display the legend
            labels: false,      // whether to display signal labels
            margins: [80, 80, 80, 120]  // canvas margins: t, r, b, l
        },
        
        // internal data binding: references the data set
        data: null,
        // internal timeout object for async. requests
        timeout: null,
        // internal color chooser
        colors: st.util.colors(),
        // SDfile SVG renderer object set for an output of 250 px by 250 px
        mol2svg: st.util.mol2svg(250, 250),
        
        /**
         * Sets the chart title option.
         *
         * @param {string} title A chart title 
         * @returns {object} the base chart
         */
        title: function (title) {
            if (title && typeof title === 'string') {
                this.opts.title = title;
            } else {
                console.log('Invalid title option.');
            }
            
            return this;
        },
        
        /**
         * Sets the chart x-axis label option.
         *
         * @param {string} xlabel A x-axis label
         * @returns {object} the base chart
         */
        xlabel: function (xlabel) {
            if (xlabel && typeof xlabel === 'string') {
                this.opts.xlabel = xlabel;
            } else {
                console.log('Invalid x-axis label option.');
            }
            return this;
        },
        
        /**
         * Sets the chart y-axis label option.
         *
         * @param {string} ylabel A y-axis label
         * @returns {object} the base chart
         */
        ylabel: function (ylabel) {
            if (ylabel && typeof ylabel === 'string') {
                this.opts.ylabel = ylabel;
            } else {
                console.log('Invalid y-axis label option.');
            }
            return this;
        },
        
        /**
         * Sets whether to reverse the x-axis.
         *
         * @param {boolean} reverse Whether to reverse the x-axis
         * @returns {object} the base chart
         */
        xreverse: function (reverse) {
            if (reverse && typeof reverse === 'boolean') {
                this.opts.xreverse = reverse;
            } else {
                console.log('Invalid x-axis reverse option.');
            }
            return this;
        },
        
        /**
         * Sets whether to reverse the y-axis.
         *
         * @param {boolean} reverse Whether to reverse the y-axis
         * @returns {object} the base chart
         */
        yreverse: function (reverse) {
            if (reverse && typeof reverse === 'boolean') {
                this.opts.yreverse = reverse;
            } else {
                console.log('Invalid y-axis reverse option.');
            }
            return this;
        },
        
        /**
         * Sets whether to display the legend.
         *
         * @param {boolean} display Whether to display the legend
         * @returns {object} the base chart
         */
        legend: function (display) {
            if (display && typeof display === 'boolean') {
                this.opts.legend = display;
            } else {
                console.log('Invalid legend option.');
            }
            return this;
        },
        
        /**
         * Sets whether to display labels.
         *
         * @param {boolean} display Whether to display labels
         * @returns {object} the base chart
         */
        labels: function (display) {
            if (display && typeof display === 'boolean') {
                this.opts.labels = display;
            } else {
                console.log('Invalid labels option.');
            }
            return this;
        },
        
        /**
         * Sets the chart margins.
         *
         * @param {int[]} margs The margins: top, right, bottom, left
         * @returns {object} the base chart
         */
        margins: function (margs) {
            if (margs && margs instanceof Array && margs.length === 4) {
               this.opts.margins = margs;
            } else {
                console.log('Invalid margins array.');
            }
            return this;
        },
        
        /**
         * Renders the base chart to the target div.
         *
         * <div id="stgraph" class="stgraph">
         *
         * |-----------------------------------|
         * |Panel                              |
         * |   |----------------------| Legend |
         * |   |Canvas                |  s1    |
         * |   |        ..            |  s2    |
         * |   |      .    .          |        |
         * |   |     .     .          |        |
         * |   |    .       ..        |        |
         * |   |  ..          ...     |        |
         * |   |----------------------|        |
         * |                                   |
         * |-----------------------------------|
         * 
         * </div>
         *
         * @params {string} x The id of the div
         */
        render: function (x) {
            // reference id of the div
            this.target = x;
            // get margin option...
            var margins = this.opts.margins;
            // ...calculate width and height of the canvas inside the panel
            this.width = $(x).width() - margins[1] - margins[3];
            this.height = $(x).height() - margins[0] - margins[2];

            // sanity check
            if (this.width <= 0) {
                console.log('Invalid chart width: ' + this.width);
                return;
            } else if (this.height <= 0) {
                console.log('Invalid chart height: ' + this.height);
                return;
            }
        
            // self-reference for nested functions
            var chart = this;
            
            // create the panel SVG element and define the base zoom behavior
            this.panel = d3.select(x)
                .append('svg:svg')
                .attr('class', 'st-base')
                .attr('width', this.width + margins[1] + margins[3])
                .attr('height', this.height + margins[0] + margins[2])
                .on('mousedown.zoom', function () { // --- mouse options ---
                    chart.mouseDown(this);
                })
                .on('mousemove.zoom', function () { // --- mouse options ---
                    chart.mouseMove(this);
                })
                .on('mouseup.zoom', function () {   // --- mouse options ---
                    chart.mouseUp();
                })
                .on('mouseout', function() {        // --- mouse options ---
                    chart.mouseOut(this);
                })
                .on('dblclick.zoom', function () {  // --- mouse options ---
                    chart.mouseDbl(this);
                });
                
            // append the chart canvas as group within the chart panel
            this.canvas = this.panel
                .append('svg:g')
                .attr('transform', 'translate(' + 
                    margins[3] + ',' + margins[0] + ')');

            // add the SVG clip path on top of the canvas
            this.canvas.append('svg:clipPath')
                .attr('id', 'clip-' + this.target)
                .append('svg:rect')
                .attr('x', 0)
                .attr('y', 0)
                .attr('width', this.width)
                .attr('height', this.height);

            // add a hidden selection rectangle
            this.selection = this.canvas.append('svg:rect')
                .attr('class', 'st-selection')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .attr('x', 0)
                .attr('y', 0)
                .attr('width', 0)
                .attr('height', 0)
                .style('pointer-events', 'none')
                .attr('display', 'none');

            // scale object with initial d3 x- and y-scale functions
            this.scales = {};
            if (this.opts.xreverse) {   // check whether axis is reversed...
                this.scales.x = d3.scale.linear()
                    .domain([1, 0])      // ...invert the x-domain limits
                    .range([0, this.width])
            } else {
                this.scales.x = d3.scale.linear()
                    .domain([0, 1])
                    .range([0, this.width])
            }
            if (this.opts.yreverse) {   // check whether axis is reversed...
                this.scales.y = d3.scale.linear()
                    .domain([1, 0])      // ...invert the y-domain limits
                    .range([this.height, 0])
            } else {
                this.scales.y = d3.scale.linear()
                    .domain([0, 1])
                    .range([this.height, 0])
            }
            
            // check if the tooltip div exists already...
            if (!$('#st-tooltips').length) {
                // add a hidden div that serves as tooltip
                this.tooltips = d3.select('body').append('div')
                    .attr('width', $(x).width())
                    .attr('height', $(x).height())
                    .style('pointer-events', 'none')
                    .attr('id', 'st-tooltips')
                    .style('position', 'absolute')
                    .style('opacity', 0);
                // split the tooltip div into a key-value pair section for
                // annotations of type st.annotation.TOOLTIP...
                this.tooltips.append('div')
                    .attr('id', 'tooltips-meta')
                    .style('height', '50%')
                    .style('width', '100%');
                // ...and a section for molecules resolved through URLs pointing
                // to SDfiles for annotations of type st.annotation.TOOLTIP_MOL
                this.tooltips.append('div')
                    .attr('id', 'tooltips-mol')
                    .style('height', '50%')
                    .style('width', '100%');
            } else { // ...reference the tooltip div if it exists
                this.tooltips = d3.select('#st-tooltips');
            }
            
            // implement custom behavior if defined in the extension
            if (typeof this.behavior == 'function') {
                this.behavior();
            }
            
            // define and render the x- and y-axis
            this.renderAxes();
            
            // draw the title
            if (this.opts.title && this.opts.title.length !== 0) {
                if (margins[0] < 20) {
                    console.log('Not enough space for chart title: ' + 
                        'increase top margin (min 20)');
                } else {
                    this.panel.append('text')
                        .attr('class', 'st-title')
                        .attr('x', margins[3] + (this.width / 2))
                        .attr('y', margins[0] * 0.75)
                        .attr('text-anchor', 'middle')
                        .attr('font-size', 'large')
                        .text(this.opts.title)
                }
            }
            
            // draw the options
            if (this.opts.labels) {
                if (margins[1] < 60) {
                    console.log('Not enough space for label option: ' + 
                        'increase right margin (min 60)');
                    return;
                }
                // create a new group element for the label option
                var labels = this.canvas.append('g')
                    .attr('id', 'st-options');
                
                // append the options title
                labels.append('text')      
                    .attr('x', this.width)
                    .attr('y', this.height - (this.height / 4))
                    .text('Options');
                
                // append the label
                var labelopt = labels.append('g');
                labelopt.append('svg:circle')
                    .attr('cx', this.width + 5)
                    .attr('cy', this.height - (this.height / 5))
                    .attr('r', 2)
                    .style('fill', '#333333')
                    .style('stroke', '#333333');
                 // append the label text
                labelopt.append('text')      
                    .attr('x', this.width + 12)
                    .attr('y', this.height - (this.height / 5) + 2)
                    .text('Labels')
                    .attr('id', 'st-label')
                    .style('cursor', 'pointer');
                // define option highlight on mouse down events
                labelopt.on('mousedown', function() { 
                    // switch the font-weight using the stroke attribute
                    var label = d3.select(this);
                    if (label.style('stroke') === 'none') {
                        label.style('stroke', '#333333');
                    } else {
                        label.style('stroke', 'none');
                    }
                    draw(chart);
                })
            }
        },
        
        /**
         * Defines and renders the x- and y-axis (direction, tick marks, etc.).
         * Axes follow standard cartesian coordinate conventions.
         */
        renderAxes: function () {
            var margins = this.opts.margins;
            // format numbers to four decimals: 1.2345678 to 1.2346
            var xFormat = d3.format(',.4g');
            // format numbers to two decimals: 1.2345678 to 1.23
            var yFormat= d3.format(',.2g');
            
            this.xaxis = d3.svg.axis()  // define the x-axis
                .scale(this.scales.x)
                .ticks(6)
                .tickSubdivide(true)
                .tickFormat(xFormat)
                .orient('bottom');
            this.yaxis = d3.svg.axis()  // define the y-axis
                .scale(this.scales.y)
                .ticks(4)
                .tickFormat(yFormat)
                .orient('left');

            this.canvas.append('svg:g') // draw the x-axis
                .attr('class', 'st-xaxis')
                .attr('transform', 'translate(0,' + this.height + ')')
                .call(this.xaxis);
            this.canvas.append('svg:g') // draw the y-axis
                .attr('class', 'st-yaxis')
                .attr('transform', 'translate(-25, 0)')
                .call(this.yaxis);

            if (this.opts.xlabel !== '') {  // draw x-label if defined
                this.panel.select('.st-xaxis').append('text')
                    .text(this.opts.xlabel)
                    .attr('text-anchor', 'middle')
                    .attr('x', this.width / 2)
                    .attr('y', margins[2] / 2);
            }
            if (this.opts.ylabel !== '') {  // draw y-label if defined
                this.panel.select('.st-yaxis').append('text')
                    .text(this.opts.ylabel)
                    .attr('transform', 'rotate (-90)')
                    .attr('text-anchor', 'middle')
                    .attr('x', 0 - this.height / 2)
                    .attr('y', 0 - margins[3] / 2);
            }
        },
        
        /**
         * Adds signal labels to the chart.
         *
         * @param {object[]} data The drawn data object
         */
        renderlabels: function (data) {
            if (!this.opts.labels) {
                return;
            }
        
            var label = this.panel.select('#st-label');
            if (label.style('stroke') === 'none' || !this.data) {
                // remove current SVG elements of the series's class
                this.canvas.selectAll('.st-labels').remove();
                return;
            }
            
            // define domain extrema in x
            var ext = [
                this.scales.x.invert(0),
                this.scales.x.invert(this.width)
            ];
            // arrange based on x-axis direction
            ext = st.util.domain(this.scales.x, ext);
            // define bin width in px
            var binwidth = 50;
            // the maximum number of bins
            var nbins = Math.ceil(this.width / binwidth);
            // the domain step size
            var step = Math.abs(ext[1] - ext[0]) / (nbins - 1);
            // local data container for labels
            var bins = [];
            
            // format numbers to two decimals: 1.2345678 to 1.23
            var format = d3.format('.2f');
            
            // define label position and label binning behavior based on
            // whether the data was binned by min or max
            var binfunc;
            var yoffset;
            if (this.data.raw.minima) {
                yoffset = 10;
                binfunc = function (y1, y2) {
                    return y1 > y2;
                };
            } else {
                yoffset = -5;
                binfunc = function (y1, y2) {
                    return y1 < y2;
                };
            }
            // keep track of the number of points
            // to calculate the averagey value
            var n = 0;
            var avg = 0;
            // iterate over all data series
            for (var i = 0; i < data.length; i++) {
                // get the series data set
                var series = data[i];  
                // get the series data accessors
                var accs = this.data.accs(i);
                // keep track of the last visited data point
                var lastdp = series[0];
                n = n + series.length;
                for (var j = 1; j < series.length; j++) {
                    var curdp = series[j];
                    var x = lastdp[accs[0]];
                    var y = lastdp[accs[1]];
                    var avg = avg + y;
                    if (binfunc(curdp[accs[1]], y)) {
                        // get the target bin
                        var bin = Math.floor((x - ext[0]) / step);
                        // get the current data point in the bin
                        var dpb = bins[bin];
                        // if the bin is already populated with a data point...
                        if (dpb) {
                            if (binfunc(dpb[accs[1]], y)) {
                                bins[bin] = lastdp;
                            }
                        // ...add the current data point to the unpopulated bin
                        } else {
                            bins[bin] = lastdp;
                        }
                    }
                    lastdp = curdp;
                }
            }
            // get average
            avg = avg / n;
            
            // remove current SVG elements of the series's class
            this.canvas.selectAll('.st-labels').remove();
            var g = this.canvas.append('g')
                .attr('class', 'st-labels')
                .attr('text-anchor', 'middle');
            var pxinv = 0;
            var pyinv = 0;
            for (var i in bins) {
                if (bins[i] && binfunc(avg, bins[i][accs[1]])) {
                    var x = bins[i][accs[0]];
                    // get the chart coordinate values for the data point
                    var xinv = this.scales.x(x);
                    var yinv = this.scales.y(bins[i][accs[1]]);
                    if (Math.abs(xinv - pxinv) < 20 &&
                        Math.abs(yinv - pyinv) < 20) {
                        pxinv = xinv;
                        pyinv = yinv;
                        continue;
                    }
                    pxinv = xinv;
                    pyinv = yinv;
                    // append the SVG text elements
                    var fill = '#333333'
                    if (yinv < 0) {
                        yinv = 0;
                        fill = 'gray';
                    }
                    g.append('text')
                        .attr('x', xinv)
                        .attr('y', yinv + yoffset)
                        .style('fill', fill)
                        .text(format(x));
                }
            }
        },
        
        /**
         * Adds annotation group accessors to the chart.
         */
        rendergroups: function () {
            if (Object.keys(this.data.raw.annoGroups).length === 0) {
                return;
            }
            
            // self-reference for nested functions
            var chart = this;
            var labels = this.canvas.select('#st-options');
            var yoffset = 0;
            if (labels[0][0] === null) {
                // create a new group element for the label option
                labels = this.canvas.append('g')
                    .attr('id', 'st-options');
                // append the options title
                labels.append('text')      
                    .attr('x', this.width)
                    .attr('y', this.height - (this.height / 4))
                    .text('Options');
            } else {
                // currently only a single option is in use
                yoffset = 15;
            }
            
            // append the label
            var labelopt = labels.append('g');
            labelopt.append('svg:circle')
                .attr('cx', this.width + 5)
                .attr('cy', this.height - (this.height / 5) + yoffset)
                .attr('r', 2)
                .style('fill', '#333333')
                .style('stroke', '#333333');
             // append the label text
            labelopt.append('text')      
                .attr('x', this.width + 12)
                .attr('y', this.height - (this.height / 5) + 2 + yoffset)
                .text('Groups')
                .attr('id', 'st-groups')
                .style('cursor', 'pointer');
            // define action on mouse up events
            labelopt.on('mouseup', function() {
                // switch the font-weight using the stroke attribute
                var label = d3.select(this);
                if (label.style('stroke') === 'none') {
                    // highlight the selected option
                    label.style('stroke', '#333333');
                    // create the popup div
                    var popup = d3.select(chart.target).append('div')
                        .attr('id', 'st-popup')
                        .style('left', d3.event.pageX + 5 + 'px')
                        .style('top', d3.event.pageY + 5 + 'px')
                        .style('opacity', 0.9)
                        .style('background-color', 'white');
                    var keys = [];
                    // populate the keys array...
                    for (var key in chart.data.raw.annoGroups) {
                        keys.push(key);
                    }
                    // ...and add to the popup div
                    popup.append('ul')
                        .selectAll('li').data(keys).enter()
                        .append('li')
                        .style('display', 'block')
                        .style('cursor', 'pointer')
                        .html(function(d) { 
                            if (chart.data.raw.annoGroups[d]) {
                                return '<strong>' + d + '</strong>';
                            } 
                            return d;
                        })
                        // action on key selection
                        .on('mousedown', function(d) { 
                            // flag the selected key, reset all others
                            for (key in chart.data.raw.annoGroups) {
                                if (key == d && !chart.data.raw.annoGroups[d]) {
                                    chart.data.raw.annoGroups[d] = 1;
                                } else {
                                    chart.data.raw.annoGroups[key] = 0;
                                }
                            }
                            // reset the chart
                            chart.mouseDbl();
                            // reset the option
                            label.style('stroke', 'none');
                            $('#st-popup').remove();
                        });   
                } else {
                    // reset the option
                    label.style('stroke', 'none');
                    $('#st-popup').remove();
                }
            });   
        },
        
        /**
         * Defines the default zoom action for mouse down events.
         * 
         * @param {object} event A mouse event
         */
        mouseDown: function (event) {
            var p = d3.mouse(event);            // get the mouse position
            var left = this.opts.margins[3];
            var top = this.opts.margins[0];
            this.panel.select('.st-selection')  // set the selection rectangle
                .attr('x', p[0] - left)         // to the mouse position on
                .attr('xs', p[0] - left)        // the canvas and make the sel-
                .attr('y', p[1] - top)          // ection rectangle visible
                .attr('ys', p[1] - top)
                .attr('width', 1)
                .attr('height', 1)
                .attr('display', 'inline');
        },

        /**
         * Defines the default zoom action for mouse move events.
         * 
         * @param {object} event A mouse event
         */
        mouseMove: function (event) {
            // get the selection rectangle
            var s = this.panel.select('.st-selection')
            if (s.attr('display') === 'inline') { // proceed only if visible
                // get the corected mouse position on the canvas
                var pointerX = d3.mouse(event)[0] - this.opts.margins[3],
                    pointerY = d3.mouse(event)[1] - this.opts.margins[0],
                    // get the width and height of the selection rectangle
                    anchorWidth = parseInt(s.attr('width'), 10),
                    anchorHeight = parseInt(s.attr('height'), 10),
                    // get the distance between the selection rectangle start
                    // coordinates and the corrected mouse position
                    pointerMoveX = pointerX - parseInt(s.attr('x'), 10),
                    pointerMoveY = pointerY - parseInt(s.attr('y'), 10),
                    // get the original start coordinates of the rectangle
                    anchorXS = parseInt(s.attr('xs'), 10),
                    anchorYS = parseInt(s.attr('ys'), 10);
                
                // update the selection rectangle...
                if ((pointerMoveX < 0 && pointerMoveY < 0) // ...quadrant II
                    || (pointerMoveX * 2 < anchorWidth
                    && pointerMoveY * 2 < anchorHeight)) {
                    s.attr('x', pointerX);
                    s.attr('width', anchorXS - pointerX);
                    s.attr('y', pointerY);
                    s.attr('height', anchorYS - pointerY);
                } else if (pointerMoveX < 0                 // ...quadrant I
                    || (pointerMoveX * 2 < anchorWidth)) {
                    s.attr('x', pointerX);
                    s.attr('width', anchorXS - pointerX);
                    s.attr('height', pointerMoveY);
                } else if (pointerMoveY < 0                 // ...quadrant I
                    || (pointerMoveY * 2 < anchorHeight)) {
                    s.attr('y', pointerY);
                    s.attr('height', anchorYS - pointerY);
                    s.attr('width', pointerMoveX);
                } else {                                    // ...quadrant IV
                    s.attr('width', pointerMoveX);
                    s.attr('height', pointerMoveY);
                }
            }
        },

        /**
         * Defines the default zoom action for mouse up events.
         */
        mouseUp: function () {
            // px threshold for selections
            var tolerance = 5; 
            // get the selection rectangle
            var selection = this.panel.select('.st-selection');
            
            // check if the px threshold has been exceeded in x and y
            if (parseInt(selection.attr('width')) > tolerance
                && parseInt(selection.attr('height')) > tolerance) {
                // get the start coordinates of the rectangle
                var x = parseFloat(selection.attr('x'));
                var y = parseFloat(selection.attr('y'));
                // get the width and height of the selection rectangle
                var width = parseFloat(selection.attr('width'));
                var height = parseFloat(selection.attr('height'));
                
                // convert the width and height to the domain range
                width = this.scales.x.invert(x + width);
                height = this.scales.y.invert(y + height);
                // convert the start coordinates to the domain range
                x = this.scales.x.invert(x);
                y = this.scales.y.invert(y);

                if (this.data) { // only act on loaded data
                    var minheight = this.data.raw.gylim[0];
                    if (height < minheight) { // sanity check
                        height = minheight;
                    }
                }

                // rescale the x and y domain based on the new values
                this.scales.x.domain([x, width]).nice();
                this.scales.y.domain([height, y]).nice();
                
                // clean up: hide the selection rectangle
                selection.attr('display', 'none');
                // clean up: re-draw the x- and y-axis
                this.canvas.select('.st-xaxis').call(this.xaxis);
                this.canvas.select('.st-yaxis').call(this.yaxis);
                // clean up: re-draw the data set
                draw(this);
            } else {
                // hide the selection rectangle
                selection.attr('display', 'none');
            }
        },
        
        /**
         * Defines the default zoom action for mouse out events.
         */
        mouseOut: function(event) {
            // get the selection rectangle
            var selection = this.panel.select('.st-selection');
            // get the mouse position
            var pointerX = d3.mouse(event)[0],
                pointerY = d3.mouse(event)[1];
            
            // hide the selection rectangle if the
            // mouse has left the panel of the chart
            if (pointerX < 0 || pointerY < 0 ||
                pointerX > $(this.target).width() ||
                pointerY > $(this.target).height()) {
                    selection.attr('display', 'none');
            }
        },

        /**
         * Defines the default zoom action for mouse double-click events.
         *
         * @param {object} event A mouse event
         */
        mouseDbl: function (event) {
            if (event) {
                // get the corected mouse position on the canvas
                var pointerX = d3.mouse(event)[0] - this.opts.margins[3],
                    pointerY = d3.mouse(event)[1] - this.opts.margins[0];
                // abort if event happened outside the canvas
                if (pointerX < 0 || pointerX > this.width ||
                    pointerY < 0 || pointerY > this.height) {
                        return;
                }
            }
        
            if (this.data === null) {   // default for empty charts
                var xdom = st.util.domain(this.scales.x, [0, 1]);
                var ydom = st.util.domain(this.scales.y, [0, 1]);
                this.scales.x.domain(xdom).nice();
                this.scales.y.domain(ydom).nice();
                this.canvas.select('.st-xaxis').call(this.xaxis);
                this.canvas.select('.st-yaxis').call(this.yaxis);
                return;
            }
            
            // reset the global x and y domain limits
            var gxlim = st.util.domain(this.scales.x, this.data.raw.gxlim);
            var gylim = st.util.domain(this.scales.y, this.data.raw.gylim);
            // rescale the x and y domains
            this.scales.x.domain(gxlim).nice();
            this.scales.y.domain(gylim).nice();
            // re-draw the x- and y-axis
            this.canvas.select('.st-xaxis').call(this.xaxis);
            this.canvas.select('.st-yaxis').call(this.yaxis);
            // re-draw the data set
            this.data.reset();
            draw(this);
        },
        
        /**
         * Defines the default tooltip action for mouse over events.
         * 
         * @param {object} event A mouse event
         * @param {object} d A series data point
         * @param {string[]} accs A series data point accessor array
         * @param {string} group An annotation group if any
         */
        mouseOverAction: function (event, d, accs, group) {
            this.tooltips   // show the tooltip
                .style('display', 'inline');
            this.tooltips   // fade in the tooltip
                .transition()
                .duration(300)
                .style('opacity', 0.9);
            // format numbers to two decimals: 1.2345678 to 1.23
            var format = d3.format('.2f');
            // get the mouse position of the event on the panel
            // var pointer = d3.mouse(event);
            // get the translated transformation matrix...
            // var matrix = event.getScreenCTM()
            //    .translate(+pointer[0], +pointer[1]);
            // ...to adjust the x- and y-position of the tooltip
            this.tooltips
                // (window.pageXOffset + matrix.e + 10)
                // (window.pageYOffset + matrix.f - 10)
                // .style('left', d3.event.clientX + 10 + 'px')
                // .style('top', d3.event.clientY - 10 + 'px')
                .style('left', d3.event.pageX + 10 + 'px')
                .style('top', d3.event.pageY - 10 + 'px')
                .style('opacity', 0.9)
                .style('border', 'dashed')
                .style('border-width', '1px')
                .style('padding', '3px')
                .style('border-radius', '10px')
                .style('z-index', '10')
                .style('background-color', 'white');
            var x = format(d[accs[0]]); // format the x value
            var y = format(d[accs[1]]); // format the y value
            // add the x and y value to the tooltip HTML
            d3.selectAll('#tooltips-meta').html(
                this.opts.xlabel + ': ' + 
                x + '<br/>' + this.opts.ylabel + ': ' + y + '<br/>'
            );
            // self-reference for nested functions
            var chart = this;
            // check whether tooltips are assigned to the series point
            if (group && group !== '' && d.annos) {
                if (!(group in d.annos)) {
                    return;
                }
                var groupannos = d.annos[group];
                // copy the tooltip-meta sub-div 
                var tooltip = d3.selectAll('#tooltips-meta').html();
                // add the tooltip key-value pairs to the tooltip HTML
                for (var key in groupannos.tooltip) {
                    tooltip += key + ': ' + groupannos.tooltip[key] + '<br/>';
                }
                // add the HTML string to the tooltip
                d3.selectAll('#tooltips-meta').html(tooltip + '<br/>');
                if (!groupannos.tooltipmol) {
                    return;
                }
                // initiate the spinner on the tooltip-mol sub-div 
                var spinner = st.util.spinner('#tooltips-meta');
                // wait 500 ms before XHR is executed
                this.timeout = setTimeout(function () {
                    // array for mol2svg XHR promises
                    var deferreds = [];
                    // hide the tooltip-mol sub-div until
                    // all promises are fulfilled
                    d3.selectAll('#tooltips-mol')
                        .style('display', 'none');
                    // resolve all SDfile URLs one by one 
                    for (var molkey in groupannos.tooltipmol) {
                        var moldivid = '#tooltips-mol-' + molkey;
                        d3.selectAll('#tooltips-mol')
                            .append('div')
                            .attr('id', 'tooltips-mol-' + molkey)
                            .style('float', 'left')
                            .style('height', '100%')
                            .style('width', '50%');
                        // draw to the tooltip-mol sub-div and assign a title
                        d3.selectAll(moldivid).html(
                            '<em>' + molkey + '</em><br/>'
                        );
                        var jqxhr = chart.mol2svg.draw(
                            groupannos.tooltipmol[molkey], moldivid);
                        deferreds.push(jqxhr);
                    }
                    // wait until all XHR promises are finished
                    $.when.apply($, deferreds).done(function () {
                        // hide the spinner
                        spinner.css('display', 'none');
                        // make the tooltip-mol sub-div visible
                        d3.selectAll('#tooltips-mol')
                            .style('display', 'inline');
                    })
                    .fail(function () {
                        // hide the spinner
                        spinner.css('display', 'none');
                    });
                }, 500);
            } else {
                // clear the tooltip-mol sub-div 
                d3.selectAll('#tooltips-mol').html('');
            }
        },
        
        /**
         * Defines the default tooltip action for mouse out events.
         */
        mouseOutAction: function () {
            // clear any timeout from an async. request
            clearTimeout(this.timeout);
            // clear the tooltip-mol sub-div 
            d3.selectAll('#tooltips-mol').html('');
            this.tooltips   // fade the tooltip
                .transition()
                .duration(300)
                .style('opacity', 0);
            this.tooltips   // hide the tooltip
                .style('display', 'none');
        },
        
        /**
         * Draws the chart legend in the top right corner.
         */
        renderLegend: function () {
            // remove the current legend
            d3.select(this.target).select('.st-legend').remove();
            // build a new div container for the legend 
            var legend = d3.select(this.target).append('div')
                .attr('class', 'st-legend')
                .style('top', -(this.height + this.opts.margins[2]) + 'px')
                .style('left', this.width + this.opts.margins[3] + 'px')
                .style('width', this.opts.margins[1] + 'px')
                .style('height', (this.height / 2) - 30 + 'px')
                .style('position', 'relative');                 
            // inner div with 'hidden' scroll bars        
            legend = legend.append('div')
                .style('position', 'absolute')
                .style('overflow', 'scroll')
                .style('top', 0 + 'px')
                .style('left', 0 + 'px')
                .style('width', this.opts.margins[1] + 30 + 'px')
                .style('bottom', -30 + 'px');

            // self-reference for nested functions
            var colors = this.colors;
            // self-reference for nested functions
            var chart = this;
            // get the length (no. of items) of the new legend
            var length = this.data.raw.series.length;
            // create a svg container
            var lg = legend.append('svg:svg')
                .attr('height', length * 20 + 'px');
            
            // iterate over all data series
            for (var i = 0; i < length; i++) {
                // get the series identifier
                var id = this.data.raw.series[i].id;
                // get the series title
                var title = this.data.raw.series[i].title;
                 // create a new group element for the data series records
                var llg = lg.append('g')
                    .attr('stid', id)
                    .style('cursor', 'pointer');
                
                // create a new group element for each series
                llg.append('svg:rect')   // append the legend symbol
                    .attr('x', 5)
                    .attr('y', function () { return i * 20; })
                    .attr('width', 10)
                    .attr('height', 10)
                    .style('fill', function () { return colors.get(id); });
                llg.append('text')       // append the data series's legend text
                    .attr('x', 20)
                    .attr('y', function () { return i * 20 + 9; })
                    .text(function () {
                        return title;
                    });
                // define series highlights on mouse over events
                llg.on('mouseover', function() { 
                    // select the series
                    d3.select(this).style('fill', 'red');
                    var selectid = d3.select(this).attr('stid');
                    // highlight the selected series
                    chart.canvas.selectAll('.' + selectid)
                        .style('stroke-width', 2);
                    // fade all other series
                    for (var dataid in chart.data.raw.ids) {
                        if (dataid !== selectid) {
                            chart.canvas.selectAll('.' + dataid)
                                .style('opacity', 0.1);
                        }
                    }
                })
                // define series highlight removal on mouse out events
                llg.on('mouseout', function() {
                    // select the series
                    d3.select(this).style('fill', 'black');
                    var selectid = d3.select(this).attr('stid');
                    // reset the selected series
                    chart.canvas.selectAll('.' + selectid)
                        .style('stroke-width', 1);
                    // reset all other series
                    for (var dataid in chart.data.raw.ids) {
                        if (dataid !== selectid) {
                            chart.canvas.selectAll('.' + dataid)
                                .style('opacity', 1);
                        }
                    }
                })
            }
        },
        
        /**
         * Loads and binds the data set to the chart.
         *
         * @param {object} data A data set
         */
        load: function (data) {
            // sanity check
            if (!data) {
                console.log('Missing data object.');
                return;
            } else if (typeof data.push !== 'function' ||
                typeof data.add !== 'function' ||
                typeof data.remove !== 'function') {
                console.log('Invalid data object.');
                return;
            }
            
            var chart = this;       // self-reference for nested functions
            this.data = data;       // associate with the chart
            var oldadd = data.add;  // copy of the old function
            data.add = function() { // redefine
                try {
                    oldadd.apply(this, arguments);   // execute old copy
                    chart.data.push(function () {    // define callback
                        chart.xscale();              // rescale x
                        chart.yscale();              // rescale y
                        chart.canvas.select('.st-xaxis')
                            .call(chart.xaxis);     // draw the x-axis
                        chart.canvas.select('.st-yaxis')
                            .call(chart.yaxis);     // draw the y-axis
                        draw(chart);
                        chart.rendergroups();           // draw the anno groups
                        if (chart.opts.legend) {
                            chart.renderLegend();   // draw the legend
                        }
                    });
                } catch (err) {
                    console.log('Data load failed: ' + err);
                }
            };
            var oldremove = data.remove;    // copy of the old function
            data.remove = function() {      // redefine
                var ids = oldremove.apply(this, arguments); // execute old copy
                // iterate over the identifiers of the removed series
                for (var i in ids) {
                    // remove color entries
                    chart.colors.remove(ids[i]);
                    // remove associated SVG elements from the canvas
                    chart.canvas.selectAll('.' + ids[i]).remove();
                }
                if (chart.opts.legend) {
                    chart.renderLegend(); // redraw the legend
                }
            };
        }
    };
};

/**
 * Draws the chart and signal labels.
 *
 * @param {object} chart A chart object
 */
function draw (chart) {
    if (typeof chart.renderdata == 'function' && 
        typeof chart.renderlabels == 'function' &&
        chart.data !== null) {
        try {
            // inefficient: store binned data?
            var data = chart.renderdata(); // draw the data set
            chart.renderlabels(data);      // draw the labels
        } catch (err) {
            chart.data.remove();
            console.log('Error rendering the data: ' + err);
        }
    }
}

/**
 * Default chart for continuous data (Chromatograms, UV/VIS, etc.). 
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @extends st.chart.chart
 * @returns {object} the continuous data chart
 */
st.chart.series = function () {
    var series = chart(); // create and extend base chart
    
    /**
     * Rescales the x domain.
     */
    series.xscale = function () {
        var array = this.data.raw.gxlim; // get global x-domain limits
        if (this.opts.xreverse) {        // check whether axis is reversed...
            array = [                    // ...invert the x-domain limits
                array[1],
                array[0]
            ];
        }
        
        this.scales.x
            .domain(array)
            .nice();
    };
    
    /**
     * Rescales the y domain.
     */
    series.yscale = function () {
        this.scales.y
            .domain(this.data.raw.gylim)
            .nice();
    };
    
    /**
     * Insertion point for custom behavior.
     */
    series.behavior = function () {
        // define a text label for selected x values in the top left corner
        this.xpointer = this.panel.append('text')
            .attr('x', this.opts.margins[3])
            .attr('y', this.opts.margins[0])
            .attr('font-size', 'x-small')
            .text('');
            
        // self-reference for nested functions
        var chart = this;
        // format numbers to four decimals: 1.2345678 to 1.2346
        var xFormat = d3.format('.4g');
        // initialise the data set reference
        this.plotted = [];
        
        // define global mouse-move behavior on the panel
        this.panel.on('mousemove', function () {
            // get the mouse position on the x scale
            var mousex = d3.mouse(this)[0] - chart.opts.margins[3];
            // get the mouse position on the x domain
            var plotx = chart.scales.x.invert(mousex);
            // get the series x domain limits
            var plotdomain = chart.scales.x.domain();
            
            if (chart.opts.xreverse) {      // check whether axis is reversed...
                var within = function () {  // ...define boundary function this
                    return plotx < plotdomain[0] && plotx >= plotdomain[1];
                }
            } else {                        
                var within = function () {  // ...or that way
                    return plotx >= plotdomain[0] && plotx < plotdomain[1];
                }
            }
            
            // check whether the mouse pointer event is within the canvas
            if (within()) {
                // set text label value to the current formatted x value
                chart.xpointer.text('x = ' + xFormat(plotx));
                
                // iterate over all data series and update the point trackers
                for (var i = 0; i < chart.plotted.length; i++) {
                    // get the series data accessors
                    var accs = chart.data.accs(i);
                    // define the series bisector function for x values
                    var bisector = d3.bisector(function (d) {
                        return d[accs[0]];
                    }).left;
                    // get the closest x index to the left 
                    // of the mouse position in the x domain
                    var j = bisector(chart.plotted[i], plotx);
                    if (j > chart.plotted[i].length - 1) { // boundary check
                        j = chart.plotted[i].length - 1;
                    }
                    // get the closest x value based on the retrieved index
                    var dp = chart.plotted[i][j];
                    if (dp) { // if defined
                        // get the mouse position on the y scale
                        var ploty = chart.scales.y(dp[accs[1]]);
                        if (ploty < 0) {    // boundary check on chart ceiling
                            ploty = 0;
                        } else if (ploty > chart.height) { // boundary check...
                            ploty = chart.height;          // ...on chart floor
                        }
                        // update the point tracker with x and y
                        chart.canvas.select('.' + chart.data.id(i) + 'focus')
                            .attr('display', 'inline')
                            .attr('transform', 'translate(' + 
                            chart.scales.x(dp[accs[0]]) + ',' + 
                             ploty + ')');
                    }
                }
            } else { // the mouse pointer event is outside the canvas...
                chart.xpointer.text(''); // ...reset the text label value
                // and hide all point trackers for each series in the data set
                for (var i = 0; i < chart.plotted.length; i++) { 
                    chart.canvas.select('.' + chart.data.id(i) + 'focus')
                        .attr('display', 'none');
                }
            }
        });
    };
    
    /**
     * Renders the data.
     *
     * @returns {object} the binned data set for the current x-axis scale
     */
    series.renderdata = function () {
        // get the binned data set for the current x-axis scale
        var data = this.data.bin(this.width, this.scales.x);
        // reference the data set for use in series.behavior
        this.plotted = data;
        // self-reference for nested functions
        var chart = this;
        // iterate over all data series
        for (var i = 0; i < data.length; i++) {
            var series = data[i];           // get the series data
            var id = this.data.id(i);       // get the series identifier
            var accs = this.data.accs(i);   // get the series data accessors
            var color = this.colors.get(id);// get the series color
            
            // define how the continuous line should be drawn
            var line = d3.svg.line()        
                .interpolate('cardinal-open') // use an open cardinal spline
                .x(function (d) {
                    return chart.scales.x(d[accs[0]]);  // x1 = x1
                })
                .y(function (d) {
                    return chart.scales.y(d[accs[1]]);  // y1 = f(x1)
                });
                
            // remove current SVG elements of the series's class
            this.canvas.selectAll('.' + id).remove();
            // create a new group for SVG elements of this series
            var g = this.canvas.append('g')
                .attr('class', id);
            
            // add a continuous line for each series
            g.append('svg:path')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .style('stroke', color)
                .style('fill', 'none')
                .style('stroke-width', 1)
                .attr('d', line(series));
            // add a single hidden circle element for point tracking
            g.append('svg:circle')
                .attr('class', id + 'focus')
                .style('stroke', color)
                .style('fill', 'none')
                .attr('r', 3)
                .attr('cx', 0)
                .attr('cy', 0)
                .attr('display', 'none')
            // add hidden circle elements for highlighting
            g.selectAll('.' + id + '.circle').data(series)
                .enter()
                .append('svg:circle')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .style('fill', color)
                .style('stroke', color)
                .attr("opacity", 0)
                .attr("r", 3)
                .attr("cx", function (d) { 
                    return chart.scales.x(d[accs[0]]) 
                })
                .attr("cy", function (d) { 
                    return chart.scales.y(d[accs[1]]) 
                })
            // define point mouse-over behavior
            .on('mouseover', function (d) {
                // highlight the selected circle
                d3.select(this).attr('opacity', 0.8);
                // call default action
                chart.mouseOverAction(this, d, accs);
            })
            // define point mouse-out behavior
            .on('mouseout', function () {
                // remove the highlight for the selected circle
                d3.select(this).attr('opacity', '0');
                // call default action
                chart.mouseOutAction();
            });
        }
        return data;
    };
    
    return series;
};

/**
 * Default chart for mass spectrometry spectra.
 *
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @extends st.chart.chart
 * @returns {object} the mass spectrometry chart
 */
st.chart.ms = function () {
    var ms = chart(); // create and extend base chart
    
    /**
     * Rescales the x domain.
     */
    ms.xscale = function () {
        this.scales.x
            .domain(this.data.raw.gxlim)
            .nice();
    };
    
    /**
     * Rescales the y domain.
     */
    ms.yscale = function () {
        this.scales.y
            .domain(this.data.raw.gylim)
            .nice();
    };
    
    /**
     * Insertion point for custom behavior.
     */
    ms.behavior = function () {
        // nothing to do
    };
    
    /**
     * Renders the data: defines how data points are drawn onto the canvas.
     *
     * @returns {object} the binned data set for the current x-axis scale
     */
    ms.renderdata = function () {
        // get the binned data set for the current x-axis scale
        var data = this.data.bin(this.width, this.scales.x);
        // get annotation group
        var group = '';
        for (var key in this.data.raw.annoGroups) {
            if (this.data.raw.annoGroups[key]) {
                group = key;
                break;
            }
        }
        // self-reference for nested functions
        var chart = this;
        // iterate over all data series
        for (var i = 0; i < data.length; i++) {
            var series = data[i];           // get the series data set
            var id = this.data.id(i);       // get the series identifier
            var accs = this.data.accs(i);   // get the series data accessors
            var color = chart.colors.get(id)// get the series color
            
            // remove current SVG elements of the series's class
            this.canvas.selectAll('.' + id).remove();
            // create a new group for SVG elements of this series
            var g = this.canvas.append('g')
                .attr('class', id);
                
            // add 'signal spikes' (lines) for each point in the data set
            g.selectAll('.' + id + '.line').data(series)
                .enter()
                .append('svg:line')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .attr('x1', function (d) { 
                    return chart.scales.x(d[accs[0]]);  // x1 = x1
                })
                .attr('y1', function (d) { 
                    return chart.scales.y(d[accs[1]]);  // y1 = f(x1)
                })
                .attr('x2', function (d) { 
                    return chart.scales.x(d[accs[0]]);  // x2 = x1
                })
                .attr('y2', chart.scales.y(0))          // y2 = 0
                .style('stroke', color)  // color by id
                .each(function(d) {      // address each point
                    if (d.annos) {  // check for on-canvas annotations...
                        if (!(group in d.annos)) {
                            return;
                        }
                        g.append('text') // ...append a SVG text element
                            .attr('class', id + '.anno')
                            .attr('x', chart.scales.x(d[accs[0]]))
                            .attr('y', chart.scales.y(d[accs[1]]) - 5)
                            .attr('text-anchor', 'middle')
                            .attr('font-size', 'small')
                            .attr('fill', color)
                            .text(d.annos[group].annotation);
                    }
                })
            // define point mouse-over behavior
            .on('mouseover', function (d) {
                // highlight the selected 'signal spike'
                d3.select(this).attr('stroke-width', 2);
                // call default action
                chart.mouseOverAction(this, d, accs, group);
            })
            // define point mouse-out behavior
            .on('mouseout', function () {
                // remove the highlight for the selected 'signal spike'
                d3.select(this).attr('stroke-width', null);
                // call default action
                chart.mouseOutAction();
            });
        }
        
        // remove current zero line element
        this.canvas.selectAll('.zeroline').remove();
        // check if the global y domain limit is lower than 0...
        if (this.data.raw.gylim[0] < 0) {
            // ...append a zero line element
            this.canvas.append('svg:line')
                .attr('class', 'zeroline')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .attr('x1', this.scales.x(this.data.raw.gxlim[0]))
                .attr('y1', this.scales.y(0))
                .attr('x2', this.scales.x(this.data.raw.gxlim[1]))
                .attr('y2', this.scales.y(0))
                .style('stroke', '#333333');
        }
        return data;
    };
    
    return ms;
};

/**
 * Default chart for infrared spectra. 
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @extends st.chart.chart
 * @returns the infrared chart
 */
st.chart.ir = function () {
    var ir = chart(); // create and extend base chart
    
    /**
     * Rescales the x domain.
     */
    ir.xscale = function () {
        this.scales.x
            .domain([   // invert the x-domain limits
                this.data.raw.gxlim[1],
                this.data.raw.gxlim[0]
            ])
            .nice();
    };
    
    /**
     * Rescales the y domain.
     */
    ir.yscale = function () {
        this.scales.y
            .domain(this.data.raw.gylim)
            .nice();
    };
    
    /**
     * Insertion point for custom behavior.
     */
    ir.behavior = function () {
        // invert the x-domain limits for initial chart setup
        this.scales.x.domain([1, 0]);
        
        // define a text label for selected x values in the top left corner
        this.xpointer = this.panel.append('text')
            .attr('x', this.opts.margins[3])
            .attr('y', this.opts.margins[0])
            .attr('font-size', 'x-small')
            .text('');
            
        // self-reference for nested functions
        var chart = this;
        // format numbers to four decimals: 1.2345678 to 1.2346
        var xFormat = d3.format('.4g');
        // initialise the data set reference
        this.plotted = [];
        
        // define global mouse-move behavior on the panel
        this.panel.on('mousemove', function () {
            // get the mouse position on the x scale
            var mousex = d3.mouse(this)[0] - chart.opts.margins[3];
            // get the mouse position on the x domain
            var plotx = chart.scales.x.invert(mousex);
            // get the series x domain limits
            var plotdomainx = chart.scales.x.domain();
            
            // check whether the mouse pointer event is within the canvas
            if (plotx < plotdomainx[0] && plotx >= plotdomainx[1]) {
                // set text label value to the current formatted x value
                chart.xpointer.text('x = ' + xFormat(plotx));
                
                // iterate over all data series and update the point trackers
                for (var i = 0; i < chart.plotted.length; i++) {
                    // get the series data accessors
                    var accs = chart.data.accs(i);
                    // define the series bisector function for x values
                    var bisector = d3.bisector(function (d) {
                        return d[accs[0]];
                    }).left;
                    // get the closest x index to the left 
                    // of the mouse position in the x domain
                    var j = bisector(chart.plotted[i], plotx);
                    if (j > chart.plotted[i].length - 1) { // boundary check
                        j = chart.plotted[i].length - 1;
                    }
                    // get the closest x value based on the retrieved index
                    var dp = chart.plotted[i][j];
                    if (dp) { // if defined
                        // get the mouse position on the y scale
                        var ploty = chart.scales.y(dp[accs[1]]);
                        if (ploty < 0) {    // boundary check on chart ceiling
                            ploty = 0;
                        } else if (ploty > chart.height) { // boundary check...
                            ploty = chart.height;          // ...on chart floor
                        }
                        // update the point tracker with x and y
                        chart.canvas.select('.' + chart.data.id(i) + 'focus')
                            .attr('display', 'inline')
                            .attr('transform', 'translate(' + 
                            chart.scales.x(dp[accs[0]]) + ',' + 
                            ploty + ')');
                    }
                }
            } else { // the mouse pointer event is outside the canvas...
                chart.xpointer.text(''); // ...reset the text label value
                // and hide all point trackers for each series in the data set
                for (var i = 0; i < chart.plotted.length; i++) {
                    chart.canvas.select('.' + chart.data.id(i) + 'focus')
                        .attr('display', 'none');
                }
            }
        });
    };
    
    /**
     * Renders the data.
     *
     * @returns {object} the binned data set for the current x-axis scale
     */
    ir.renderdata = function () {
        // get the binned data set for the current x-axis scale
        // param: true -> bin by y value minima
        var data = this.data.bin(this.width, this.scales.x, true);
        // get annotation group
        var group = '';
        for (var key in this.data.raw.annoGroups) {
            if (this.data.raw.annoGroups[key]) {
                group = key;
                break;
            }
        }
        // dirty hack: set flag to indicate binning by minima
        // used in the chart object for label placement
        this.data.raw.minima = 1;
        // reference the data set for use in series.behavior
        this.plotted = data;
        // self-reference for nested functions
        var chart = this;
        // iterate over all data series
        for (var i = 0; i < data.length; i++) {
            var series = data[i];           // get the series data
            var id = this.data.id(i);       // get the series identifier
            var accs = this.data.accs(i);   // get the series data accessors
            var color = this.colors.get(id);// get the series color
            
            // define how the continuous line should be drawn
            var line = d3.svg.line()
                .interpolate('cardinal-open')
                .x(function (d) {
                    return chart.scales.x(d[accs[0]]);
                })
                .y(function (d) {
                    return chart.scales.y(d[accs[1]]);
                });
                
            // remove current SVG elements of the series's class
            this.canvas.selectAll('.' + id).remove();
            // create a new group for SVG elements of this series
            var g = this.canvas.append('g')
                .attr('class', id);
                
            // add a continuous line for each series
            g.append('svg:path')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .style('stroke', color)
                .style('fill', 'none')
                .attr('d', line(series));
            // add a single hidden circle element for point tracking
            g.append('svg:circle')
                .attr('class', id + 'focus')
                .style('stroke', color)
                .style('fill', 'none')
                .attr('r', 3)
                .attr('cx', 0)
                .attr('cy', 0)
                .attr('display', 'none')
            // add hidden circle elements for highlighting
            g.selectAll('.' + id + '.circle').data(series)
                .enter()
                .append('svg:circle')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .style('fill', color)
                .style('stroke', color)
                .attr("opacity", 0)
                .attr("r", 3)
                .attr("cx", function (d) { 
                    return chart.scales.x(d[accs[0]]) 
                })
                .attr("cy", function (d) { 
                    return chart.scales.y(d[accs[1]]) 
                })
                .each(function(d) {      // address each point
                    if (d.annos) {  // check for on-canvas annotations...
                        if (!(group in d.annos)) {
                            return;
                        }
                        g.append('text') // ...append a SVG text element
                            .attr('class', id + '.anno')
                            .attr('x', chart.scales.x(d[accs[0]]))
                            .attr('y', chart.scales.y(d[accs[1]]) + 20)
                            .attr('text-anchor', 'middle')
                            .attr('font-size', 'small')
                            .attr('fill', color)
                            .text(d.annos[group].annotation);
                    }
                })
            // define point mouse-over behavior
            .on('mouseover', function (d) {
                // highlight the selected circle
                d3.select(this).attr('opacity', 0.8);
                // call default action
                chart.mouseOverAction(this, d, accs, group);
            })
            // define point mouse-out behavior
            .on('mouseout', function () {
                // remove the highlight for the selected circle
                d3.select(this).attr('opacity', '0');
                // call default action
                chart.mouseOutAction();
            });
        }
        return data;
    };
    
    return ir;
};

/**
 * Default chart for NMR spectra. 
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @extends st.chart.chart
 * @returns the NMR chart
 */
st.chart.nmr = function () {
    var nmr = chart(); // create and extend base chart
    
    /**
     * Renders the base chart to the target div.
     *
     * <div id="stgraph" class="stgraph">
     *
     * |-----------------------------------|
     * |Panel                              |
     * |   |----------------------| Legend |
     * |   |Canvas                |  s1    |
     * |   |        ..            |  s2    |
     * |   |      .    .          |        |
     * |   |     .     .          |        |
     * |   |    .       ..        |        |
     * |   |  ..          ...     |        |
     * |   |----------------------|        |
     * |                                   |
     * |-----------------------------------|
     * 
     * </div>
     *
     * @params {string} x The id of the div
     */
    nmr.render = function (x) {
        // reference id of the div
        this.target = x;
        // get margin option...
        var margins = this.opts.margins;
        // ...calculate width and height of the canvas inside the panel
        this.width = $(x).width() - margins[1] - margins[3];
        this.height = $(x).height() - margins[0] - margins[2];
    
        // sanity check
        if (this.width <= 0) {
            console.log('Invalid chart width: ' + this.width);
            return;
        } else if (this.height <= 0) {
            console.log('Invalid chart height: ' + this.height);
            return;
        }
    
        // self-reference for nested functions
        var chart = this;

        // scale object with initial d3 x- and y-scale functions
        this.scales = { 
            x: d3.scale.linear()
                .domain([1, 0]) // invert the x-domain limits
                .range([0, this.width]),
            y: d3.scale.linear()
                .range([this.height, 0])
        };
        
        // create the panel SVG element and define the base zoom behavior
        this.panel = d3.select(x)
            .append('svg:svg')
            .attr('class', 'st-base')
            .attr('width', this.width + margins[1] + margins[3])
            .attr('height', this.height + margins[0] + margins[2]);
        // define the base zoom behavior
        init_mouse(chart);
         
        // append the chart canvas as group within the chart panel
        this.canvas = this.panel
            .append('svg:g')
            .attr('transform', 'translate(' + margins[3] + ',' + margins[0] + ')');

        // add the SVG clip path on top of the canvas
        this.canvas.append('svg:clipPath')
            .attr('id', 'clip-' + this.target)
            .append('svg:rect')
            .attr('x', 0)
            .attr('y', 0)
            .attr('width', this.width)
            .attr('height', this.height);

        // add a hidden selection rectangle
        this.selection = this.canvas.append('svg:rect')
            .attr('class', 'st-selection')
            .attr('clip-path', 'url(#clip-' + this.target + ')')
            .attr('x', 0)
            .attr('y', 0)
            .attr('width', 0)
            .attr('height', 0)
            .style('pointer-events', 'none')
            .attr('display', 'none');
        
        // define and render the x- and y-axis
        this.renderAxes();
        
        // draw the title
        if (this.opts.title && this.opts.title.length !== 0) {
            if (margins[0] < 20) {
                console.log('Not enough space for chart title: ' + 
                    'increase top margin (min 20)');
            } else {
                this.panel.append('text')
                    .attr('class', 'st-title')
                    .attr('x', margins[3] + (this.width / 2))
                    .attr('y', margins[0] * 0.75)
                    .attr('text-anchor', 'middle')
                    .attr('font-size', 'large')
                    .text(this.opts.title)
            }
        }
        
        // draw the options
        if (this.opts.labels) {
            if (margins[1] < 60) {
                console.log('Not enough space for label option: ' + 
                    'increase right margin (min 60)');
                return;
            }
            // create a new group element for the label option
            var labels = this.canvas.append('g')
                .attr('id', 'st-options');
            
            // append the options title
            labels.append('text')      
                .attr('x', this.width)
                .attr('y', this.height - (this.height / 4))
                .text('Options');
            
            // append the label
            var labelopt = labels.append('g');
            labelopt.append('svg:circle')
                .attr('cx', this.width + 5)
                .attr('cy', this.height - (this.height / 5))
                .attr('r', 2)
                .style('fill', '#333333')
                .style('stroke', '#333333');
             // append the label text
            labelopt.append('text')      
                .attr('x', this.width + 12)
                .attr('y', this.height - (this.height / 5) + 2)
                .text('Labels')
                .attr('id', 'st-label')
                .style('cursor', 'pointer');
            // define option highlight on mouse down events
            labelopt.on('mousedown', function() { 
                // switch the font-weight using the stroke attribute
                var label = d3.select(this);
                if (label.style('stroke') === 'none') {
                    label.style('stroke', '#333333');
                } else {
                    label.style('stroke', 'none');
                }
                draw(chart);
            })
        }
        
        return this;
    };
    
    /**
     * Rescales the x domain.
     */
    nmr.xscale = function () {
        this.scales.x
            .domain([
                this.data.raw.gxlim[1],
                this.data.raw.gxlim[0]
            ])
            .nice();
    };
    
    /**
     * Rescales the y domain.
     */
    nmr.yscale = function () {
        this.scales.y
            .domain(this.data.raw.gylim);
    };
    
    /**
     * Defines and renders the x-axis (direction, tick marks, etc.).
     * Axes follow standard cartesian coordinate conventions.
     */
    nmr.renderAxes = function () {
        var margins = this.opts.margins;
        // format numbers to four decimals: 1.2345678 to 1.2346
        var xFormat = d3.format('.4g');
        
        this.xaxis = d3.svg.axis()  // define the x-axis
            .scale(this.scales.x)
            .ticks(6)
            .tickSubdivide(true)
            .tickFormat(xFormat)
            .orient('bottom');

        this.canvas.append('svg:g') // draw the x-axis
            .attr('class', 'st-xaxis')
            .attr('transform', 'translate(0,' + this.height + ')')
            .call(this.xaxis);

        if (this.opts.xlabel !== '') {   // draw x-label if defined
            this.panel.select('.st-xaxis').append('text')
                .text(this.opts.xlabel)
                .attr('text-anchor', 'middle')
                .attr('x', this.width / 2)
                .attr('y', margins[2] / 2);
        }
    };
    
    /**
     * Defines the default zoom action for mouse down events.
     * 
     * @param {object} event A mouse event
     */
    nmr.mouseDown = function (event) {
        var p = d3.mouse(event);
        var left = this.opts.margins[3];
        this.panel.select('.st-selection')  // set the selection rectangle
            .attr('x', p[0] - left)         // to the mouse position on
            .attr('xs', p[0] - left)        // the canvas and make the sel-
            .attr('width', 1)               // ection rectangle visible
            .attr('height', this.height)
            .attr('display', 'inline');
    };
    
    /**
     * Defines the default zoom action for mouse move events.
     * 
     * @param {object} event A mouse event
     */
    nmr.mouseMove = function (event) {
        // get the selection rectangle
        var s = this.panel.select('.st-selection')
        if (s.attr('display') === 'inline') { // proceed only if visible
            // get the corected mouse position (x) on the canvas
            var pointerX = d3.mouse(event)[0] - this.opts.margins[3],
                // get the width of the selection rectangle
                anchorWidth = parseInt(s.attr('width'), 10),
                // get the distance between the selection rectangle start
                // coordinates and the corrected mouse position in x
                pointerMoveX = pointerX - parseInt(s.attr('x'), 10),
                // get the original start coordinates of the rectangle
                anchorXS = parseInt(s.attr('xs'), 10);
             
            // update the selection rectangle
            if (pointerMoveX < 1 && (pointerMoveX * 2) < anchorWidth) {
                s.attr('x', pointerX);
                s.attr('width', anchorXS - pointerX);
            } else {
                s.attr('width', pointerMoveX);
            }
        }
    };

    /**
     * Defines the default zoom action for mouse up events.
     */
    nmr.mouseUp = function () {
        // px threshold for selections
        var tolerance = 5;
        // get the selection rectangle
        var selection = this.panel.select('.st-selection');
        
        // check if the px threshold has been exceeded in x
        if (parseInt(selection.attr('width')) > tolerance) {
            // get the x start coordinate of the rectangle
            var x = parseFloat(selection.attr('x'));
            // get the width of the selection rectangle
            var width = parseFloat(selection.attr('width'));

            // convert the width to the domain range
            width = this.scales.x.invert(x + width);
            // convert the x start coordinate to the domain range
            x = this.scales.x.invert(x);
            
            // rescale the x domain based on the new values
            this.scales.x.domain([x, width]).nice();

            // clean up: hide the selection rectangle
            selection.attr('display', 'none');
            // clean up: re-draw the x-axis
            this.canvas.select('.st-xaxis').call(this.xaxis);
            // clean up: re-draw the data set
            draw(this);
        } else {
            // hide the selection rectangle
            selection.attr('display', 'none');
        }
    };
    
    /**
     * Defines the default zoom action for mouse double-click events.
     */
    nmr.mouseDbl = function (event) {
        if (event) {
            // get the corected mouse position on the canvas
            var pointerX = d3.mouse(event)[0] - this.opts.margins[3],
                pointerY = d3.mouse(event)[1] - this.opts.margins[0];
            // abort if event happened outside the canvas
            if (pointerX < 0 || pointerX > this.width ||
                pointerY < 0 || pointerY > this.height) {
                    return;
            }
        }
    
        if (this.data === null) {   // default for empty charts
            this.scales.x.domain([1, 0]).nice();
            this.scales.y.domain([0, 1]).nice();
            this.canvas.select('.st-xaxis').call(this.xaxis);
            return;
        }
    
        // rescale the x and y domains
        this.scales.x.domain([
            this.data.raw.gxlim[1],
            this.data.raw.gxlim[0]
        ]).nice();
        this.scales.y.domain(this.data.raw.gylim);
        // re-draw the x-axis
        this.canvas.select('.st-xaxis').call(this.xaxis);
        // re-draw the data set
        draw(this);
    };
    
    /**
     * Loads and binds the data set to the chart.
     *
     * @param {object} data A data set
     */
    nmr.load = function (data) {
        // sanity check
        if (!data) {
            console.log('Missing data object.');
            return;
        } else if (typeof data.push !== 'function' ||
            typeof data.add !== 'function' ||
            typeof data.remove !== 'function') {
            console.log('Invalid data object.');
            return;
        }
        
        var chart = this;       // self-reference for nested functions
        this.data = data;       // associate with the chart
        var oldadd = data.add;  // copy of the old function
        data.add = function() { // redefine
            oldadd.apply(this, arguments);  // execute old copy
            chart.data.push(function () {   // define callback
                chart.xscale();             // rescale x
                chart.yscale();             // rescale y
                init_mouse(chart);          // re-initialise the mouse behavior      
                chart.canvas.select('.st-xaxis')
                    .call(chart.xaxis);     // draw the x-axis   
                draw(chart);
                chart.rendergroups();           // draw the anno groups
                if (chart.opts.legend) {
                    chart.renderLegend();   // draw the legend
                }
            });
        };
        var oldremove = data.remove;    // copy of the old function
        data.remove = function() {      // redefine
            var ids = oldremove.apply(this, arguments); // execute old copy
            // iterate over the identifiers of the removed series
            for (var i in ids) {
                // remove color entries
                chart.colors.remove(ids[i]);
                // remove associated SVG elements from the canvas
                chart.canvas.selectAll('.' + ids[i]).remove();
            }
            if (chart.opts.legend) {
                chart.renderLegend(); // redraw the legend
            }
        };
    };
    
    /**
     * Renders the data.
     *
     * @returns {object} the binned data set for the current x-axis scale
     */
    nmr.renderdata = function () {
        // get the binned data set for the current x-axis scale
        var data = this.data.bin(this.width, this.scales.x);
        // get annotation group
        var group = '';
        for (var key in this.data.raw.annoGroups) {
            if (this.data.raw.annoGroups[key]) {
                group = key;
                break;
            }
        }    
        // self-reference for nested functions
        var chart = this;
        
        // iterate over all data series
        for (var i = 0; i < data.length; i++) {
            var series = data[i];           // get the series data
            var id = this.data.id(i);       // get the series identifier
            var accs = this.data.accs(i);   // get the series data accessors
            
            // define how the continuous line should be drawn
            var line = d3.svg.line()
                .x(function (d) {
                    return chart.scales.x(d[accs[0]]);
                })
                .y(function (d) {
                    return chart.scales.y(d[accs[1]]);
                });
                
            // remove current SVG elements of the series's class
            this.canvas.selectAll('.' + id).remove();
            // create a new group for SVG elements of this series
            var g = this.canvas.append('g')
                .attr('class', id);
            
            // add a continuous line for each series
            g.append('svg:path')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .style('stroke', this.colors.get(id))
                .style('fill', 'none')
                .style('stroke-width', 1)
                .attr('d', line(series));
            g.data(series).each(function(d) {      // address each point
                    if (d.annos) {  // check for on-canvas annotations...
                        if (!(group in d.annos)) {
                            return;
                        }
                        g.append('text') // ...append a SVG text element
                            .attr('class', id + '.anno')
                            .attr('x', chart.scales.x(d[accs[0]]))
                            .attr('y', chart.scales.y(d[accs[1]]) - 5)
                            .attr('text-anchor', 'middle')
                            .attr('font-size', 'small')
                            .attr('fill', color)
                            .text(d.annos[group].annotation);
                    }
                });
        }
        return data;
    };
    
    return nmr;
};

/**
 * Defines the base zoom behavior.
 *
 * @param {object} chart A chart object
 */
function init_mouse (chart) {
    var mousewheel = d3.behavior.zoom() // the mouse wheel zoom behavior
        .y(chart.scales.y)
        .center([0, chart.scales.y(0)])
        .on("zoom", function() {
            draw(chart);
        });
    chart.panel.call(mousewheel)
        .on('mousedown.zoom', function () { // --- mouse options ---
            chart.mouseDown(this);
        })
        .on('mousemove.zoom', function () { // --- mouse options ---
            chart.mouseMove(this);
        })
        .on('mouseup.zoom', function () {   // --- mouse options ---
            chart.mouseUp();
        })
        .on('mouseout', function() {        // --- mouse options ---
            chart.mouseOut(this);
        })
        .on('dblclick.zoom', function () {  // --- mouse options ---
            chart.mouseDbl(this);
        })
}

/**
 * Default chart for 2D NMR spectra. 
 * 
 * @author Stephan Beisken <beisken@ebi.ac.uk>
 * @constructor
 * @extends st.chart.chart
 * @returns {object} the 2D NMR chart
 */
st.chart.nmr2d = function () {
    var nmr2d = chart(); // create and extend base chart
    
    /**
     * Rescales the x domain.
     */
    nmr2d.xscale = function () {
        this.scales.x
            .domain([   // invert the x-domain limits
                this.data.raw.gxlim[1],
                this.data.raw.gxlim[0]
            ])
            .nice();
    };
    
    /**
     * Rescales the y domain.
     */
    nmr2d.yscale = function () {
        this.scales.y
            .domain([
                this.data.raw.gylim[1],
                this.data.raw.gylim[0]
            ])
            .nice();
    };
    
    /**
     * Insertion point for custom behavior.
     */
    nmr2d.behavior = function () {
        // invert the x- and y-domain limits for initial chart setup
        this.scales.x.domain([1, 0]);
        this.scales.y.domain([1, 0]);
        
        // append rectangle of width 1 to serve as ruler in x
        var selX = this.canvas.append('svg:rect')
            .attr('class', 'st-selection')
            .attr('y', 0)
            .attr('width', 1)
            .attr('height', this.height)
            .style('pointer-events', 'none')
            .attr('visibility', 'hidden');
        // append rectangle of width 1 to serve as ruler in y
        var selY = this.canvas.append('svg:rect')
            .attr('class', 'st-selection')
            .attr('x', 0)
            .attr('width', this.width)
            .attr('height', 1)
            .style('pointer-events', 'none')
            .attr('visibility', 'hidden');
            
        // self-reference for nested functions
        var that = this;
        // define axis ruler actions for mouse move events
        d3.select('.st-base').on('mousemove', function () {
            // get the corrected mouse position on the canvas
            var pointerX = d3.mouse(this)[0] - that.opts.margins[3];
            var pointerY = d3.mouse(this)[1] - that.opts.margins[0];
            // whether the mouse event is outside the canvas...
            if (pointerX < 0 || pointerX > that.width
                || pointerY < 0 || pointerY > that.height) {
                selX.attr('visibility', 'hidden');
                selY.attr('visibility', 'hidden');
            // ...or inside the canvas: set rulers visible
            } else {
                selX.attr('x', pointerX);
                selY.attr('y', pointerY);
                selX.attr('visibility', 'visible');
                selY.attr('visibility', 'visible');
            }
            }) // append invisible rectangle to capture ruler events
            .append('svg:rect')
            .attr('class', 'st-mouse-capture')
            .style('visibility', 'hidden')
            .attr('x', 0)
            .attr('y', 0)
            .attr('width', this.width)
            .attr('height', this.height);
    };
    /**
     * Defines and renders the x- and y-axis (direction, tick marks, etc.).
     * Axes follow standard cartesian coordinate conventions.
     */
    nmr2d.renderAxes = function () {
        var margins = this.opts.margins;
        // format numbers to three decimals: 1.2345678 to 1.235
        var xFormat = d3.format('.3g');
        
        this.xaxis = d3.svg.axis()  // define the x-axis
            .scale(this.scales.x)
            .ticks(6)
            .tickSubdivide(true)
            .tickFormat(xFormat)
            .tickSize(-this.height)
            .tickPadding(5)
            .orient('bottom');
        this.yaxis = d3.svg.axis()  // define the y-axis
            .scale(this.scales.y)
            .ticks(6)
            .tickFormat(xFormat)
            .tickSize(-this.width)
            .tickPadding(5)
            .orient('right');

        this.canvas.append('svg:g') // draw the x-axis
            .attr('class', 'st-xaxis')
            .attr('transform', 'translate(0,' + this.height + ')')
            .call(this.xaxis);
        this.canvas.append('svg:g') // draw the y-axis
            .attr('class', 'st-yaxis')
            .attr('transform', 'translate(' + this.width + ',0)')
            .call(this.yaxis);

        if (this.opts.xlabel !== '') {  // draw x-label if defined
            d3.select('.st-xaxis').append('text')
                .text(this.opts.xlabel)
                .attr('text-anchor', 'middle')
                .attr('x', this.width / 2)
                .attr('y', margins[2] / 2);
        }
        if (this.opts.ylabel !== '') {  // draw y-label if defined
            d3.select('.st-yaxis').append('text')
                .text(this.opts.ylabel)
                .attr('transform', 'rotate (-90)')
                .attr('text-anchor', 'middle')
                .attr('x', 0 - this.height / 2)
                .attr('y', margins[1] / 2);
        }
    };
    
    /**
     * Renders the data.
     */
    nmr2d.renderdata = function () {
        // get the unbinned data set for the current x-axis scale
        var data = this.data.get(this.width, this.scales.x);
        // self-reference for nested functions
        var chart = this;
        // iterate over all data series
        for (var i = 0; i < data.length; i++) {
            var series = data[i];           // get the series data
            var id = this.data.id(i);       // get the series identifier
            var accs = this.data.accs(i);   // get the series data accessors
            
            // remove current SVG elements of the series's class
            this.canvas.selectAll('.' + id).remove();
            // create a new group for SVG elements of this series
            var g = this.canvas.append('g')
                .attr('class', id);
            
            // add circles for each series
            g.selectAll('.' + id + '.circle').data(series)
                .enter()
                .append('svg:circle')
                .attr('clip-path', 'url(#clip-' + this.target + ')')
                .style('fill', this.colors.get(id))
                .style('stroke', this.colors.get(id))
                .attr("r", 3)
                .attr("cx", function (d) { 
                    return chart.scales.x(d[accs[0]]) 
                })
                .attr("cy", function (d) { 
                    return chart.scales.y(d[accs[1]]) 
                })
            // define point mouse-over behavior
            .on('mouseover', function (d) {
                // call default action
                chart.mouseOverAction(this, d, accs);
            })
            // define point mouse-out behavior
            .on('mouseout', function () {
                // call default action
                chart.mouseOutAction();
            });
        }
    };
    
    return nmr2d;
};

if (typeof define === 'function' && define.amd) {
    define(st);
} else if (typeof module === 'object' && module.exports) {
    module.exports = st;
} else {
    this.st = st;
}
}();
