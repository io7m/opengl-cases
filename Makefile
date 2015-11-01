all: vao0 vao1 vao_leak0 vao_leak1

vao0: vao0.c
	cc -o vao0 -W -Wall -pedantic -std=c99 vao0.c -lglfw -lGL

vao1: vao1.c
	cc -o vao1 -W -Wall -pedantic -std=c99 vao1.c -lglfw -lGL

vao_leak0: vao_leak0.c
	cc -o vao_leak0 -W -Wall -pedantic -std=c99 vao_leak0.c -lglfw -lGL

vao_leak1: vao_leak1.c
	cc -o vao_leak1 -W -Wall -pedantic -std=c99 vao_leak1.c -lglfw -lGL

clean:
	rm -f vao0 vao1 vao_leak0 vao_leak1
