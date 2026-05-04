import zlib
import struct
import os

def make_png(color_rgb, filename):
    os.makedirs(os.path.dirname(filename), exist_ok=True)
    width = 16
    height = 16
    raw_data = b''
    for y in range(height):
        raw_data += b'\x00'
        for x in range(width):
            raw_data += bytes(color_rgb) + b'\xff'

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

# Tachi blade - #b87333 (miedź)
make_png([0xb8, 0x73, 0x33], f'{base}/tachi_blade.png')
print("tachi_blade.png - #b87333 (miedź)")

# Odachi blade - złoto #FFD700
make_png([0xFF, 0xD7, 0x00], f'{base}/odachi_blade.png')
print("odachi_blade.png - #FFD700 (złoto)")
