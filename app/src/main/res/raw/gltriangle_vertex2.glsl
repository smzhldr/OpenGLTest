attribute vec4 a_position;

uniform mat4 v_matrix;

attribute vec4 a_color;

varying vec4 v_color;

void main(){
    gl_Position = v_matrix*a_position;
    v_color=a_color;
}