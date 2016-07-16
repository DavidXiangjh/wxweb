
if (typeof EMP == "undefined") 
{
     var EMP = {};
}


EMP.namespace = function() {
    var a=arguments, o=null, i, j, d;
    for (i=0; i<a.length; i=i+1) 
    {
        d=a[i].split(".");
        o=EMP;

        // EMP is implied, so it is ignored if it is included
        for (j=(d[0] == "EMP") ? 1 : 0; j<d.length; j=j+1) {
            o[d[j]]=o[d[j]] || {};
            o=o[d[j]];
        }
    }

    return o;
};

EMP.init = function() {
    this.namespace("widget", "util");
};


EMP.init();

