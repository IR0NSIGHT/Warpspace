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

float rand(vec2 n) {
    return fract(cos(dot(n, vec2(12.9898, 4.1414))) * 43758.5453);
}

float noise(vec2 n) {
    const vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n), f = smoothstep(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

float fbm(vec2 n) {
    float total = 0.0, amplitude = 1.0;
    for (int i = 0; i < 4; i++) {
        total += noise(n) * amplitude;
        n += n;
        amplitude *= 0.5;
    }
    return total;
}
vec3 create(float x, float y, float z){
    vec3 stthing = vec3(x,y,z) * 11.0;
    vec3 color = vec3(0);

    vec3 fv = normalize(flightVel) * 0.25;
    float flightSpeed = length(flightVel)/maxSpeed; //between 0 and 1 based on max speed
    float dotVel = dot(fv,normalize(vertPos));
    float proxToWarpVector = 0.5 + (dotVel * 0.5);
    float attFactor = smoothstep(0.,1.,min(flightSpeed*8.,1.)); //quick ramp to full 1

    float attProx = smoothstep(1.,proxToWarpVector,attFactor); //Attenuated flight orientation effect so that there won't be a sudden transition at 0 speed to some other state
    float bgRingFunc = (0.95+0.05*attFactor) * sin(afreq*1.7452 * ((time * 0.5 * attFactor) + attProx));

    vec3 weirdmathq = vec3(0);

    weirdmathq.x = fbm(stthing.xy + vec2(0.35*time,13.*time + bgRingFunc * 0.1));
    weirdmathq.y = fbm(stthing.yz + vec2(0.5*time,10.*time + bgRingFunc));
    weirdmathq.z = fbm(stthing.zx + vec2(0.44 * time + (bgRingFunc*0.4),0.6465742 + 3.1*time));

    vec3 weirdmathr = vec3(0);

    weirdmathr.x = fbm(stthing.xz + weirdmathq.xz + vec2(1.7,9.2) + 2.315*time);
    weirdmathr.y = fbm(stthing.yx + weirdmathq.yx + vec2(8.3,2.8) + 3.126*time);
    weirdmathr.z = fbm(stthing.zy + weirdmathq.zy + vec2(9.5,4.6) + 2.731*time);

    float f = fbm(mix(stthing.xy,stthing.zx,0.5 + (0.5 * sin(0.1*time))));

    color = mix(vec3(0.101961,0.619608,0.666667),
    vec3(0.666667,0.666667,0.498039),
    clamp((f*f)*4.0,0.0,1.0));

    color = mix(color,
    vec3(0.3,0.1,0),
    clamp(length(weirdmathq),0.0,1.0));

    color = mix(color,
    vec3(0.666667,0.13,0),
    clamp(weirdmathr.x,0.0,1.0));

    color.xyz *= (f*f*f+.6*f*f+.5*f);
    //TODO: Where is that static BG coming from??
    return color;
}

float sinus(vec3 pos, float period, float amplitude)
{
    //3 sinus functions overlaying, each with higher frequency and smaller amplitude
    float point =(sin(pos.x*period/6.3)+sin(pos.y*period/6.3)+sin(pos.z*period/6.3))*(1./3.)*amplitude;
    return point;
}

void main()
{
    time = (timeBasis/80.);// * 1 + (someVeryLargeNumber * (1-warpDepth));
    float t = time * 7.;
    vec4 color = ambient;
    vec3 loc = normalize(vertPos);


    vec3 colorA = vec3(0.0, 0.0, 0.0);
    vec3 colorB = vec3(1.0, 0.0, 0.702);

    vec3 flightDir = normalize(flightVel);
    vec3 pxNormal = normalize((-1.)*vertPos); //cam centered sphere => pos = normal
    float pct = abs(smoothstep(0.,0.25,dot(flightDir,pxNormal)));

    pct *= length(flightVel)/400.;

    vec3 finalColor = mix(colorA, colorB, pct);


    gl_FragColor = vec4(finalColor, 1);//vec4(c*color.rgb, 1.0);
}
