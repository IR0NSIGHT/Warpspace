#version 120

varying vec4 ambient;
varying vec3 normal;
varying vec3 vert;
varying vec3 vertPos;
float time; //internal time; rate can vary

uniform float timeBasis;
uniform float warpDepth; //TODO
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
    vec3 stthing = vec3(x,y,z) * 11;
    vec3 color = vec3(0);

    vec3 fv = normalize(flightVel) * 0.25;
    float flightSpeed = length(flightVel)/maxSpeed; //between 0 and 1 based on max speed
    float dotVel = dot(fv,normalize(vertPos));
    float proxToWarpVector = 0.5 + (dotVel * 0.5);
    float attFactor = smoothstep(0,1,min(flightSpeed*8,1)); //quick ramp to full 1

    float attProx = smoothstep(1,proxToWarpVector,attFactor); //Attenuated flight orientation effect so that there won't be a sudden transition at 0 speed to some other state
    float bgRingFunc = (0.95+0.05*attFactor) * sin(afreq*1.7452 * ((time * 0.5 * attFactor) + attProx));

    vec3 weirdmathq = vec3(0);

    weirdmathq.x = fbm(stthing.xy + vec2(0.35*time,13*time + bgRingFunc * 0.1));
    weirdmathq.y = fbm(stthing.yz + vec2(0.5*time,10*time + bgRingFunc));
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

void main()
{
    time = (timeBasis/80);// * 1 + (someVeryLargeNumber * (1-warpDepth));
    float t = time * 7;
    vec4 color = ambient;
    vec3 loc = normalize(vertPos);

    color -= 0.3;

    color *= gl_Color;

    float flightSpeed = length(flightVel)/maxSpeed;
    vec3 flightDir = normalize(flightVel); //normalized heading
    vec3 c = create(loc.x,loc.y,loc.z);

    flightDir *= -1; //rings should progress opposite the direction of travel
    float dotVel = dot(flightDir,loc);
    float proxToWarpVector = 0.5 + (dotVel * 0.5);
    float smoothprox = smoothstep(0,1,proxToWarpVector);
    float peakprox = pow(proxToWarpVector,8);
    float opp = 1 - proxToWarpVector; //distance from backward vector
    //float localFrequency = (afreq + (0.1 * pow(opp,0.25))); //up to 0.2 extra frequency if far enough behind
    float flightSpeedMult = 1 - (0.07 * flightSpeed); //greatly reduced range to avoid rapid flashing at high speed (and create an impression of distance)
    float localTime = flightSpeedMult * t;// * (0.8 + (opp * 0.2)); //rate that time passes can reduce 20% depending on distance from heading point... oor could, but that was probably causing runaway pile-up

    float vRingFunc = flightSpeed * sin(afreq * (localTime + (smoothprox * flightSpeedMult)));

    float foreAftReduction = pow(smoothstep(0,1,(abs(min(flightSpeed*8,1)*dotVel) * pow(flightSpeed,0.125))),4); //maybe remove pow idk
    float vRingFinal = vRingFunc * 0.08 * (1-foreAftReduction); //increased orig. amplitude (0.031) because of fore-aft reduction mechanic

    vec3 finalColor = c.rgb * color.rgb * 0.6;

    finalColor += 0.05 * opp;
    finalColor.r += 0.2;
    finalColor.g += min(1,pow(proxToWarpVector,12)) * flightSpeed * 0.3;
    finalColor.b += smoothprox * flightSpeed * 0.2;

    finalColor = normalize(finalColor);

    finalColor.r = 1 - pow(finalColor.r,6);
    finalColor.gb *= 0.1;
    finalColor.b += finalColor.g * 1.25;

    finalColor.r += 0.3 * vRingFinal;
    finalColor.r *= 0.7;
    finalColor.g += 0.2 * vRingFinal;
    finalColor.rgb -= 0.4 * vRingFinal;
    //TODO:
    // -shift colours and alpha slightly by speed
    // -increase time multiplier, increase brightness, and drop alpha when leaving warp (and do reverse when entering) ... should be nonlinear between 0 and 1; will have to do some function fitting in Desmos. potentially just x^6

    gl_FragColor = vec4(finalColor, 1);//vec4(c*color.rgb, 1.0);
}
