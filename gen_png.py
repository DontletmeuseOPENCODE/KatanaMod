import zlib
import struct

def make_png(color_rgb, filename):
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
        # IHDR: width, height, bitdepth, colortype, compression, filter, interlace
        f.write(make_chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)))
        f.write(make_chunk(b'IDAT', compressed))
        f.write(make_chunk(b'IEND', b''))

make_png([192, 192, 192], '/home/szym/.minecraft/resourcepacks/KatanaModTexturePack/assets/minecraft/textures/item/katanas/zk_blade.png')
make_png([101, 67, 33], '/home/szym/.minecraft/resourcepacks/KatanaModTexturePack/assets/minecraft/textures/item/katanas/zk_base.png')
