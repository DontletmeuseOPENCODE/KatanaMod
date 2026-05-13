import zlib
import struct
import os

def make_premium_shuriken(filename):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    width = 16
    height = 16
    canvas = [[0]*16 for _ in range(16)]
    
    # Colors: 0: trans, 1: dark gray (#4F4F4F), 2: silver (#C0C0C0), 3: white/shine (#FFFFFF)
    colors = {
        0: [0, 0, 0, 0],
        1: [79, 79, 79, 255],
        2: [192, 192, 192, 255],
        3: [255, 255, 255, 255]
    }

    # Center circle (hole)
    center_points = [(7,7), (7,8), (8,7), (8,8)]
    
    # Body (diamond shape)
    for y in range(4, 12):
        for x in range(4, 12):
            if abs(y-7.5) + abs(x-7.5) <= 5:
                canvas[y][x] = 2
                
    # Blades
    # Top
    for i in range(1, 5):
        canvas[i][7] = 1; canvas[i][8] = 2
    canvas[0][7] = 3; canvas[0][8] = 3
    # Bottom
    for i in range(11, 15):
        canvas[i][7] = 2; canvas[i][8] = 1
    canvas[15][7] = 3; canvas[15][8] = 3
    # Left
    for i in range(1, 5):
        canvas[7][i] = 2; canvas[8][i] = 1
    canvas[7][0] = 3; canvas[8][0] = 3
    # Right
    for i in range(11, 15):
        canvas[7][i] = 1; canvas[8][i] = 2
    canvas[7][15] = 3; canvas[8][15] = 3

    # Add a hole in middle
    for py, px in center_points:
        canvas[py][px] = 0

    # Shine/Edges
    canvas[4][7] = 3; canvas[7][4] = 3; canvas[11][8] = 3; canvas[8][11] = 3

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
make_premium_shuriken(f'{base}/shuriken_blade.png')
print("Wygenerowano teksturę Shurikena V3 (Premium Metallic)")
