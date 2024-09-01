from Parser import ParserBase


class ParserStr(ParserBase):

    def next(self, token, tokenType):
        if token == "\n":
            self.onToken.next("", "new-line")
        else:
            for s in token:
                if s == "\n":
                    self.onToken.next("", "end-line")
                else:
                    self.onToken.next(s, "text")

