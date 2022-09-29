//#version 120

//SOURCES:
//-----------------------------------------------------------------------
// -https://www.shadertoy.com/view/MlfBRB (for warp tunnel noise algorithm & helper functions)
///*
// * "Hyperspace" by Ben Wheatley - 2017
// * License MIT License
// * Contact: github.com/BenWheatley
// */
//-----------------------------------------------------------------------
// -https://www.shadertoy.com/view/MtcGRl (for nonmoving noise algorithm)
// Credit to Luke Rissacher, no license provided.

varying vec4 ambient;
varying vec3 normal;
varying vec3 vert;
varying vec3 vertPos;
float time; //internal time; rate can vary

uniform float timeBasis;
uniform float distortedTime;
const float noiseScrollTimeCoeff = 0.05; //multiplier for distorted time

uniform float warpDepth;
uniform vec3 vesselOrigin; //in world pos of ship
uniform vec3 flightDir;
uniform float absoluteSpeed;
uniform float maxSpeed;
const float afreq = 125.0; //maximum ring frequency for warp velocity indicator
varying float mainRadius;

const int MAX_OCTAVE = 10;
const float PI = 3.14159265359;
const float thetaToPerlinScale = 2.0 / PI;

float cosineInterpolate(float a, float b, float x) {
    float ft = x * PI;
    float f = (1.0 - cos(ft)) * 0.5;

    return a*(1.0-f) + b*f;
}

float seededRandom(float seed) {
    int x = int(seed);
    x = x << 13 ^ x;
    x = (x * (x * x * 15731 + 789221) + 1376312589);
    x = x & 0x7fffffff;
    return float(x)/1073741824.0;
}

float planeAngle(vec3 p1a, vec3 p1b, vec3 p2a, vec3 p2b){
    vec3 normal1 = normalize(cross(normalize(p1a),normalize(p1b)));
    vec3 normal2 = normalize(cross(normalize(p2a),normalize(p2b)));
    float result = acos(dot(normal1,normal2))/PI * 180;
    return result;
}

float perlinNoise(float theta, float r, float time) { //polar(ish) noise from warptunnel demo
    float sum = 0.0;
    for (int octave=0; octave<MAX_OCTAVE; ++octave) {
        float sf = pow(2.0, float(octave));
        float sf8 = sf*128.0;

        float new_theta = sf*theta;
        float new_r = sf*r/4.0 + time; // Add current time to this to get an animated effect

        float new_theta_floor = floor(new_theta);
        float new_r_floor = floor(new_r);
        float fraction_r = new_r - new_r_floor;
        float fraction_theta = new_theta - new_theta_floor;

        float t1 = seededRandom( new_theta_floor	+	sf8 *  new_r_floor    );
        float t2 = seededRandom( new_theta_floor	+	sf8 * (new_r_floor+1.0));

        new_theta_floor += 1.0;
        float maxVal = sf*2.0;
        if (new_theta_floor >= maxVal) {
            new_theta_floor -= maxVal; // So that interpolation with angle 0-360° doesn't do weird things with angles > 360°
        }

        float t3 = seededRandom( new_theta_floor	+	sf8 *  new_r_floor      );
        float t4 = seededRandom( new_theta_floor	+	sf8 * (new_r_floor+1.0) );

        float i1 = cosineInterpolate(t1, t2, fraction_r);
        float i2 = cosineInterpolate(t3, t4, fraction_r);

        sum += cosineInterpolate(i1, i2, fraction_theta)/sf;
    }
    return 2.0*sum;
}

float hash(vec3 p)  // replace this by something better
{
    p  = fract( p*0.3183099+.1 );
    p *= 17.0;
    return fract( p.x*p.y*p.z*(p.x+p.y+p.z) );
}

vec2 GetGradient(vec2 intPos, float t) {  // Uncomment for calculated rand
    float rand = fract(sin(dot(intPos, vec2(12.9898, 78.233))) * 43758.5453);;

    float angle = 6.283185 * rand + 4.0 * t * rand;    // Rotate gradient: random starting rotation, random rotation rate
    return vec2(cos(angle), sin(angle));
}

//cartesian noise
float pnoise(float x, float y, float z) {
    vec3 pos = vec3(x,y,z);
    vec2 i = floor(pos.xy);
    vec2 f = pos.xy - i;
    vec2 blend = f * f * (3.0 - 2.0 * f);
    float noiseVal =
    mix(
    mix(
    dot(GetGradient(i + vec2(0, 0), pos.z), f - vec2(0, 0)),
    dot(GetGradient(i + vec2(1, 0), pos.z), f - vec2(1, 0)),
    blend.x),
    mix(
    dot(GetGradient(i + vec2(0, 1), pos.z), f - vec2(0, 1)),
    dot(GetGradient(i + vec2(1, 1), pos.z), f - vec2(1, 1)),
    blend.x),
    blend.y
    );
    return noiseVal / 0.7; // normalize to about [-1..1]
}

float staticNoise(vec3 p, float time)
{
    float a = pnoise(p.x,p.y,time);
    float b = pnoise(p.y,p.z,time);
    float c = pnoise(p.z,p.x,time);

    return pnoise(a,b,c);
}

float ithnoise(vec3 vertPos, float posCoeff, float time){
    posCoeff += sin(time)*0.3f;

    float cumuNoise = 0.0; //cumulative noise value
    float posCoeffFinal = 0.5*posCoeff;
    for(int i=0;i<8;i++){
        float n = pow(0.5,float(i));
        float s = abs(staticNoise((vertPos * posCoeffFinal)/(n+1),time * n * 4));
        cumuNoise += s;
        cumuNoise = abs(pow(cumuNoise,1.-s));
    }
    cumuNoise = cos(cumuNoise);
    return cumuNoise;
}

float pnoiseq(float x, float y, float t) { //Unused alternate warp noise function
    return 0.5+(0.5*sin(ithnoise(vec3(x*9,y*3,t/100.),1.,t)));
}

void main()
{
    time = (timeBasis/80.);// * 1 + (someVeryLargeNumber * (1-warpDepth));//BEGIN OLD ITHI VARIABLES
    float relativeSpeed = max(5,absoluteSpeed)/maxSpeed; //between 0 and 1 based on max speed
   // relativeSpeed *= warpDepth; //auto-animate acceleration into/out of warp regardless of actual speed

    //constants
    vec3 redColor = vec3(1,0.,0.);
    vec3 magentaColor = vec3(1,0.,1);

//control values
    //how well the vert is aligned to flight dir. 0=right angle to flightdir, 1=aligned with flightdir
    float alignedToFlightDir = abs(dot(normalize(vertPos), flightDir));

    float smoothAligned = min(1,mix(1,alignedToFlightDir*alignedToFlightDir,relativeSpeed*2)) ;   //the slower you go, the less factors goes towards 1
    //modifier for how hectic the noise fluctuation is supposed to be
    float noiseChangeSpeed = mix(0.2,3, smoothAligned);


    //calculate the base color for background and noise
    float timedFluctuation = sin(time*0.5)*0.1;
    vec3 baseColor = mix(redColor, magentaColor, relativeSpeed+timedFluctuation);

//add block tunnels at flight direction
    //more detailed noise for fastships, more hectic noise for verts aligned to flightdir with quadratic falloff
    vec3 noiseColor = baseColor * ithnoise(
        vertPos,
        relativeSpeed,  //how big the noiseclouds, higher value = bigger
        sin(time*noiseChangeSpeed)); //how hectic, higher value = faster

    //noise in flightdir, baseColor to the sides, with a quadratic falloff from flightdir
    //noiseColor = mix(baseColor,noiseColor,alignedToFlightDir*alignedToFlightDir);
    gl_FragColor = vec4(noiseColor, 1.0);//vec4(c*color.rgb, 1.0);
}
