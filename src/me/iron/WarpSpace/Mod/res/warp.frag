//#version 120

varying vec4 ambient;
varying vec3 normal;
varying vec3 vert;
varying vec3 vertPos;
float time; //internal time; rate can vary

uniform float timeBasis;
uniform float warpDepth; //TODO
uniform vec3 vesselOrigin; //in world pos of ship
uniform vec3 flightVel; //TODO
uniform float maxSpeed;
const float afreq = 14.5; //maximum ring frequency for warp velocity indicator
varying float mainRadius;

const int MAX_OCTAVE = 8;
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
    return acos(dot(normalize(cross(p1a,p1b)),normalize(cross(p2a,p2b)))); //ohgods
}

float perlinNoise(float theta, float r, float time) {
    float sum = 0.0;
    for (int octave=0; octave<MAX_OCTAVE; ++octave) {
        float sf = pow(2.0, float(octave));
        float sf8 = sf*64.0;

        float new_theta = sf*theta;
        float new_r = sf*r/4.0 + time; // Add current time to this to get an animated effect

        float new_theta_floor = floor(new_theta);
        float new_r_floor = floor(new_r);
        float fraction_r = new_r - new_r_floor;
        float fraction_theta = new_theta - new_theta_floor;

        float t1 = seededRandom( new_theta_floor	+	sf8 *  new_r_floor      );
        float t2 = seededRandom( new_theta_floor	+	sf8 * (new_r_floor+1.0) );

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


void main()
{
    //BEGIN OLD ITHI VARIABLES
    vec3 fv = normalize(flightVel) * 0.25;
    float flightSpeed = length(flightVel)/maxSpeed; //between 0 and 1 based on max speed
    float dotVel = dot(fv,normalize(vertPos));
    float proxToWarpVector = 0.5 + (dotVel * 0.5);
    float attFactor = smoothstep(0.,1.,min(flightSpeed*8.,1.)); //quick ramp to full 1

    float attProx = smoothstep(1.,proxToWarpVector,attFactor); //Attenuated flight orientation effect so that there won't be a sudden transition at 0 speed to some other state
    float bgRingFunc = (0.95+0.05*attFactor) * sin(afreq*1.7452 * ((time * 0.5 * attFactor) + attProx));
    //END OLD ITHI VARIABLES

    time = (timeBasis/80.);// * 1 + (someVeryLargeNumber * (1-warpDepth));
    float t = time * 7.;
    vec4 color = ambient;
    vec3 loc = normalize(vertPos);

    vec3 colorForward = vec3(0.96, 0.63, 0.02);
    vec3 colorAft = vec3(0.07,0.0,0.35);
    vec3 colorDark = vec3(0.0,0.001,0.01);

    vec3 shiftColor = mix(colorForward,colorAft,proxToWarpVector);
    vec3 refY = vec3(0,1,0); //reference UP vector
    float theta = 0.5 * planeAngle(/*plane 1*/normalize(flightVel),refY,/*plane 2*/normalize(flightVel),normalize(vertPos));
    theta += 0.01;
    float noiseRaw = perlinNoise(theta,(1-proxToWarpVector)*50.,timeBasis * flightSpeed * 0.5);
    float noiseAdj = pow((noiseRaw/2) - 0.5,1.1); //todo: adj

    //vec3 finalColor = vec3(noiseAdj,noiseAdj*0.64,0.05);//TODO: add everything else into this
    vec3 finalColor = mix(colorDark,shiftColor,noiseAdj);
    gl_FragColor = vec4(finalColor, 1);//vec4(c*color.rgb, 1.0);
    /*krap


    vec3 flightDir = normalize(flightVel);
    vec3 pxNormal = normalize((-1.)*vertPos); //cam centered sphere => pos = normal
    float pct = 1 - abs(smoothstep(0.,0.25,dot(flightDir,pxNormal)));

    pct *= length(flightVel)/400.;

    float extremeness = 1 - pow(abs(dot(flightDir,pxNormal)),3);
    colorForward = mix(colorForward,colorDark,extremeness);
    colorAft = mix(colorAft,colorDark,extremeness);
    vec3 finalColor = mix(colorForward, colorAft, pct);
    finalColor *= 0.25;
    finalColor.rg = tunnel(time*0.05*(1+length(flightVel)),0);
    finalColor.g *= 0.7;

    gl_FragColor = vec4(finalColor, 1);//vec4(c*color.rgb, 1.0);
    */
}
