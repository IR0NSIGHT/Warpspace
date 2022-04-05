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
uniform vec3 flightVel;
uniform float maxSpeed;
const float afreq = 125; //maximum ring frequency for warp velocity indicator
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
    float result = acos(dot(normal1,normal2));
    if(sqrt((normal1.x*normal1.x) + (normal1.z*normal1.z)) > sqrt((normal2.x*normal2.x) + (normal2.z*normal2.z))) result = PI - result;
    return result * 0.49999;
}

//polar(ish) noise from warptunnel demo
float perlinNoise(float theta, float r, float time) {
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

vec2 GetGradient(vec2 intPos, float t) {

    // Uncomment for calculated rand
    float rand = fract(sin(dot(intPos, vec2(12.9898, 78.233))) * 43758.5453);;

    // Rotate gradient: random starting rotation, random rotation rate
    float angle = 6.283185 * rand + 4.0 * t * rand;
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
    float cumuNoise = 0; //cumulative noise value
    float posCoeffFinal = 0.5*posCoeff;
    for(int i=0;i<8;i++){
        float n = pow(0.5,i);
        float s = abs(staticNoise((vertPos * posCoeffFinal)/(n+1),time * n * 10));
        cumuNoise += s;
        cumuNoise = abs(pow(cumuNoise,1-s));
    }
    cumuNoise = cos(cumuNoise);
    return cumuNoise;
}

float pnoiseq(float x, float y, float t) { //Unused alternate warp noise function
    return 0.5+(0.5*sin(ithnoise(vec3(x*9,y*3,t/100),1,t)));
}

void main()
{
    time = (timeBasis/80.);// * 1 + (someVeryLargeNumber * (1-warpDepth));
    //BEGIN OLD ITHI VARIABLES
    vec3 fv = - (normalize(flightVel));
    float BLOOM_THRESHOLD = 0.75; //SM has weird pure white blooms when you try to approach full brightness too closely

    float flightSpeed = length(flightVel)/maxSpeed; //between 0 and 1 based on max speed
    flightSpeed *= warpDepth; //auto-animate acceleration into/out of warp regardless of actual speed
    float dotVel = dot(fv,normalize(vertPos));
    float proxToWarpVector = 0.5 + (dotVel * 0.5);
    float attFactor = smoothstep(0.,1.,min(flightSpeed*10,1)); //quick ramp to full 1

    float attProx = smoothstep(1.,proxToWarpVector,attFactor); //Attenuated flight orientation effect so that there won't be a sudden transition at 0 speed to some other state
    //float bgRingFunc = (0.95+0.05*attFactor) * sin(afreq*1.7452 * ((time * 0.5 * attFactor) + attProx));
    float bgRingFunc = (0.5 * sin(afreq * (-proxToWarpVector + (time * -0.5 * attFactor)))) + 0.5;
    bgRingFunc = pow(bgRingFunc,mix(40,30,flightSpeed)); //thin rings
    bgRingFunc = mix(0,bgRingFunc,attFactor); //rings fade out at very low speed (range of one-eighth or less)
    bgRingFunc = mix(bgRingFunc,0,min(1,abs(dotVel)*mix(5,2,flightSpeed))); //rings fade out ahead and behind
    //END OLD ITHI VARIABLES

    float t = time * 7.;
    vec4 color = ambient;
    vec3 loc = normalize(vertPos);

    vec3 colorForward = vec3(0.96, 0.58, 0.02); //only relevant at intermediate speeds
    vec3 colorAft = vec3(0.07,0.0,0.8); //only relevant at intermediate speeds
    vec3 colorDark = vec3(0.0,0.001,0.01);
    vec3 cyan = vec3(0.1,0.95,1.0);
    vec3 yellow = vec3(1.0,0.9,0);
    vec3 red = vec3(0.96,0.00,0.05);
    vec3 lightcloud = vec3(1.0,0.925,0.811);
    vec3 bluwhite = vec3(0.94,0.945,1.0);
    vec3 redblack = vec3(0.08,0,0);

    vec3 shiftColor = mix(colorForward,colorAft,proxToWarpVector);
    vec3 refY = vec3(0,1,0); //reference UP vector
    float theta = planeAngle(/*plane 1*/flightVel,refY,/*plane 2*/flightVel,vertPos);
    //TODO: This math in planeAngle is wrong. I end up getting coordinates that mirror...
    //TODO: I suspect I accidentally made a setup that doesn't respect angles above pi
    //theta *= 0.5;
    theta -= 0.05; //may or may not remove a weird seam

    float timeAdj = timeBasis * 0.31;
    timeAdj += 1 - (0.1 * proxToWarpVector); //idk if this does anything
    //TODO: By this way comes floating-point imprecision, and in time, decay and ruin in the form of increasingly bad noise quality and banding. Not sure how to fix this.

    float r = proxToWarpVector;
    float maximumPower = mix(10,2,flightSpeed);
    r = pow(r,mix(maximumPower,1,pow(1-r,flightSpeed)));
    r *= 5.5; //correct value to avoid 'squished' or 'stretched' noise

    r+=(distortedTime * noiseScrollTimeCoeff); //somewhat better forward progression this way
    float noise1 = perlinNoise(theta,r,69.420 + (timeAdj * 0.6)); //timeBasis * flight speed was nice at constant speeds, but made rapidly-shifting noise on accel to the point of being an accessibility concern
    noise1 = (noise1/(2*PI)); //formerly 1.5 //todo: adjust awful maths
    //noise1 = sin(noise1); //generates sparse clouds
    noise1 = pow(noise1,3.5);
    noise1 *= BLOOM_THRESHOLD;

    float noise2 = perlinNoise(theta,r,42.0 + timeAdj*0.25);
    noise2 = abs(sin(noise2));
    noise2 = pow(noise2,5);
    noise2 *= BLOOM_THRESHOLD;

    float noise3 = perlinNoise(theta,r,timeAdj*0.35);
    noise3 = abs(cos(noise3));
    noise3 = pow(noise3,4);
    noise3 *= BLOOM_THRESHOLD;

    float noise4 = perlinNoise(theta,r,timeAdj*0.5);
    noise4 = abs(sin(noise4));
    noise4 = pow(noise4,4);
    noise4 *= BLOOM_THRESHOLD;

    vec3 finalColor = mix(colorDark,shiftColor,noise1);

    vec3 color2 = mix(red,yellow,noise2);
    color2 = mix(color2,lightcloud,pow(noise2,11));
    color2 = mix(color2,cyan,pow(noise2,22));

    finalColor = mix(finalColor,color2,noise2);
    finalColor = mix(finalColor,cyan,noise3*0.7*pow(flightSpeed,3));

    finalColor = mix(finalColor,lightcloud,noise4*0.6*flightSpeed*flightSpeed);

    finalColor = mix(length(finalColor) * red,finalColor,pow(flightSpeed,0.5));
    finalColor.b = finalColor.b * flightSpeed;
    finalColor.r = finalColor.r * (1 - (0.1*flightSpeed));

    finalColor.r = mix(finalColor.r,0.24,flightSpeed*bgRingFunc*0.2);
    finalColor.b = mix(finalColor.b,1.0,flightSpeed*bgRingFunc*0.2);
    finalColor.g = mix(finalColor.g,0.6,flightSpeed*bgRingFunc*0.2);

    float tunnelEndsExp = mix(2100,1984,flightSpeed*flightSpeed);

    bluwhite *= 0.9; //stupid bloom threshold forces me to reduce the intensity a bit or it just turns white
    //SM y u do dis
    finalColor = mix(finalColor,finalColor*0.05,pow(proxToWarpVector,tunnelEndsExp));
    finalColor = mix(finalColor,bluwhite,pow(proxToWarpVector,tunnelEndsExp)); //white part ahead of ship

    vec3 stoppedInWarp = vec3(0.7,0,0);

    float speedMixture = mix(warpDepth,1-dotVel,pow(attFactor,0.5)); //need coordinate warping ahead and behind, not in flanking doughnut
    //scaling coefficient varying with speeds and proximity to flight vector (if there is a flight vector at all)
    //neat-looking and unifying transition out of stationary - coordinate system warps ahead and behind as speed increases
    float staticVFX = max(0,1 - ithnoise(vertPos,speedMixture,time));
    vec3 staticColor = mix(
                            vec3(0.7,0,0),
                            mix(vec3(0.7,0,0),vec3(0.7,0.075,0),warpDepth), //orangey peaks fade away as ships slip from warp
                            pow(3,staticVFX)
    );

    stoppedInWarp = mix(staticColor,vec3(0,0,0),staticVFX);

    //TODO: Replace this with formalized octaves. At least eight of them.

    finalColor = mix(stoppedInWarp,finalColor,attFactor);

    finalColor = mix(finalColor,redblack,pow(1-proxToWarpVector,tunnelEndsExp*0.05)); //black part behind ship

    gl_FragColor = vec4(finalColor, 1);//vec4(c*color.rgb, 1.0);
}
