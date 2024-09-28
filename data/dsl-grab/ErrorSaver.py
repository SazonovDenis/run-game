import os


class ErrorSaver():
    outDirName = None

    errors = None

    def handleError(self, token, error):
        print(token["text"])
        self.errors.write("ERROR: " + error + "\n")
        self.errors.write("token.text: " + token["text"] + "\n")
        self.errors.write("\n")
        self.errors.write("\n")

    def open(self, outDirName):
        self.outDirName = outDirName

        os.makedirs(self.outDirName, exist_ok=True)

        self.errors = open(self.outDirName + "errors.parse.txt", "w", encoding="utf-8")

        print("out directory: " + self.outDirName)
        print()

    def close(self):
        self.errors.close()

        print()
        print("write done")
