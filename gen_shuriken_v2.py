import zlib
import struct
import os

def make_shuriken_png(filename):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    width = 16
    height = 16
    # 0 = transparent, 1 = dark blue (#00008B), 2 = light gray (#D3D3D3) for edges
    # A simple 4-pointed star
    canvas = [[0]*16 for _ in range(16)]
    
    # Center
    for i in range(7, 9):
        for j in range(7, 9):
            canvas[i][j] = 1
            
    # Points
    for i in range(4, 12):
        canvas[i][7] = 1
        canvas[i][8] = 1
        canvas[7][i] = 1
        canvas[8][i] = 1
        
    # Tips
    canvas[3][7] = 2; canvas[3][8] = 2
    canvas[12][7] = 2; canvas[12][8] = 2
    canvas[7][3] = 2; canvas[8][3] = 2
    canvas[7][12] = 2; canvas[8][12] = 2

    raw_data = b''
    colors = {
        0: [0, 0, 0, 0],
        1: [0, 0, 139, 255],
        2: [211, 211, 211, 255]
    }

    for y in range(height):
        raw_data += b'\x00' # filter type
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

base = '/home/szym/.minecraft/resourcepacks/KatanaModTexturePack/assets/minecraft/textures/item/katanas'
make_shuriken_png(f'{base}/shuriken_blade.png')
print("Wygenerowano nową teksturę Shurikena (2D Star)")
