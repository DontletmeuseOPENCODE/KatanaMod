import zlib
import struct
import os

def make_smoke_bomb_png(filename):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    width = 16
    height = 16
    canvas = [[0]*16 for _ in range(16)]
    
    # Colors: 0: trans, 1: light gray (#E0E0E0), 2: white (#FFFFFF), 3: dark gray (#A0A0A0)
    colors = {
        0: [0, 0, 0, 0],
        1: [224, 224, 224, 255],
        2: [255, 255, 255, 255],
        3: [160, 160, 160, 255]
    }

    # Draw a swirling ball
    for y in range(16):
        for x in range(16):
            dist = ((x-7.5)**2 + (y-7.5)**2)**0.5
            if dist < 6:
                if dist < 2:
                    canvas[y][x] = 2
                elif (x+y) % 3 == 0:
                    canvas[y][x] = 1
                else:
                    canvas[y][x] = 3
            if dist < 1:
                canvas[y][x] = 0 # hole in center for effect

    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'
        for x in range(width):
            raw_data += bytes(colors[canvas[y][x]])

    compressor = zlib.compressobj()
    compressed = compressor.compress(raw_data)
    compressed += compressor.flush()

    def make_chunk(ctype, data):
        return struct.pack('>I', len(data)) + ctype + data + struct.pack('>I', zlib.crc32(ctype + data) & 0xffffffff)

    with open(filename, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        f.write(make_chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)))
        f.write(make_chunk(b'IDAT', compressed))
        f.write(make_chunk(b'IEND', b''))

base = '/home/szym/.minecraft/resourcepacks/KatanaModTexturePack/assets/minecraft/textures/item'
make_smoke_bomb_png(f'{base}/smoke_bomb.png')
print("Wygenerowano teksturę Bomby Dymnej (Wind Charge Style)")
